package com.packflow.service;

import com.packflow.dao.CustomerDAO;
import com.packflow.dao.DispatchDAO;
import com.packflow.dao.InventoryDAO;
import com.packflow.dao.InventoryTransactionDAO;
import com.packflow.dao.OrderDAO;
import com.packflow.dao.OrderItemDAO;
import com.packflow.dao.PackagingServiceDAO;
import com.packflow.dao.PackagingTaskDAO;
import com.packflow.dao.QualityCheckDAO;
import com.packflow.dao.impl.CustomerDAOImpl;
import com.packflow.dao.impl.DispatchDAOImpl;
import com.packflow.dao.impl.InventoryDAOImpl;
import com.packflow.dao.impl.InventoryTransactionDAOImpl;
import com.packflow.dao.impl.OrderDAOImpl;
import com.packflow.dao.impl.OrderItemDAOImpl;
import com.packflow.dao.impl.PackagingServiceDAOImpl;
import com.packflow.dao.impl.PackagingTaskDAOImpl;
import com.packflow.dao.impl.QualityCheckDAOImpl;
import com.packflow.exception.DatabaseException;
import com.packflow.exception.InsufficientInventoryException;
import com.packflow.exception.InvalidOrderException;
import com.packflow.exception.InvalidStatusTransitionException;
import com.packflow.model.DispatchRecord;
import com.packflow.model.DispatchStatus;
import com.packflow.model.InventoryItem;
import com.packflow.model.InventoryTransaction;
import com.packflow.model.Order;
import com.packflow.model.OrderItem;
import com.packflow.model.OrderStatus;
import com.packflow.model.PackagingServiceItem;
import com.packflow.model.PackagingTask;
import com.packflow.model.QualityCheck;
import com.packflow.model.QualityStatus;
import com.packflow.model.TaskStatus;
import com.packflow.model.TransactionType;
import com.packflow.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core Business Workflow Engine managing Order creation, pricing calculations,
 * atomic inventory reservations via JDBC Transactions, and strict state transitions.
 */
public class OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final InventoryDAO inventoryDAO;
    private final InventoryTransactionDAO inventoryTransactionDAO;
    private final PackagingServiceDAO packagingServiceDAO;
    private final PackagingTaskDAO packagingTaskDAO;
    private final QualityCheckDAO qualityCheckDAO;
    private final DispatchDAO dispatchDAO;
    private final CustomerDAO customerDAO;

    public OrderService() {
        this.orderDAO = new OrderDAOImpl();
        this.orderItemDAO = new OrderItemDAOImpl();
        this.inventoryDAO = new InventoryDAOImpl();
        this.inventoryTransactionDAO = new InventoryTransactionDAOImpl();
        this.packagingServiceDAO = new PackagingServiceDAOImpl();
        this.packagingTaskDAO = new PackagingTaskDAOImpl();
        this.qualityCheckDAO = new QualityCheckDAOImpl();
        this.dispatchDAO = new DispatchDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
    }

    public OrderService(OrderDAO orderDAO, OrderItemDAO orderItemDAO, InventoryDAO inventoryDAO,
                        InventoryTransactionDAO inventoryTransactionDAO, PackagingServiceDAO packagingServiceDAO,
                        PackagingTaskDAO packagingTaskDAO, QualityCheckDAO qualityCheckDAO,
                        DispatchDAO dispatchDAO, CustomerDAO customerDAO) {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.inventoryDAO = inventoryDAO;
        this.inventoryTransactionDAO = inventoryTransactionDAO;
        this.packagingServiceDAO = packagingServiceDAO;
        this.packagingTaskDAO = packagingTaskDAO;
        this.qualityCheckDAO = qualityCheckDAO;
        this.dispatchDAO = dispatchDAO;
        this.customerDAO = customerDAO;
    }

    public Optional<Order> getOrderById(int orderId) {
        Optional<Order> opt = orderDAO.findById(orderId);
        if (opt.isPresent()) {
            Order order = opt.get();
            order.setItems(orderItemDAO.findByOrderId(orderId));
            packagingTaskDAO.findByOrderId(orderId).ifPresent(order::setTask);
            qualityCheckDAO.findByOrderId(orderId).ifPresent(order::setQualityCheck);
            dispatchDAO.findByOrderId(orderId).ifPresent(order::setDispatch);
        }
        return opt;
    }

    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = orderDAO.findByCustomerId(customerId);
        for (Order o : orders) {
            o.setItems(orderItemDAO.findByOrderId(o.getOrderId()));
        }
        return orders;
    }

    public List<Order> getPaginatedOrders(int page, int pageSize, String search, Integer customerId,
                                          OrderStatus status, Date fromDate, Date toDate) {
        int offset = Math.max(0, (page - 1) * pageSize);
        List<Order> list = orderDAO.findPaginated(offset, pageSize, search, customerId, status, fromDate, toDate);
        for (Order o : list) {
            o.setItems(orderItemDAO.findByOrderId(o.getOrderId()));
        }
        return list;
    }

    public int getTotalOrderCount(String search, Integer customerId, OrderStatus status, Date fromDate, Date toDate) {
        return orderDAO.countTotal(search, customerId, status, fromDate, toDate);
    }

    /**
     * Creates a new Packaging Order and its line items.
     * Enforces server-side recalculation of unit prices and totals.
     */
    public Order createOrder(Order order, List<OrderItem> items) throws InvalidOrderException {
        if (order.getCustomerId() <= 0) {
            throw new InvalidOrderException("Customer ID is required to create an order.");
        }
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one line item.");
        }

        // Server-Side Pricing Verification
        BigDecimal computedSubtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException("Item quantity must be strictly greater than 0.");
            }
            if (item.getProductId() <= 0) {
                throw new InvalidOrderException("Valid product must be selected for all items.");
            }

            // Retrieve verified service price from database
            Optional<PackagingServiceItem> serviceOpt = packagingServiceDAO.findById(item.getServiceId());
            if (serviceOpt.isEmpty()) {
                throw new InvalidOrderException("Invalid packaging service ID: " + item.getServiceId());
            }

            BigDecimal verifiedUnitPrice = serviceOpt.get().getBasePrice();
            item.setUnitPrice(verifiedUnitPrice);
            item.calculateTotalPrice();
            computedSubtotal = computedSubtotal.add(item.getTotalPrice());
        }

        order.setSubtotal(computedSubtotal);
        order.setTotalCost(computedSubtotal);
        order.setStatus(OrderStatus.PENDING);
        if (order.getOrderDate() == null) {
            order.setOrderDate(Date.valueOf(LocalDate.now()));
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int orderId = orderDAO.create(order, conn);
            if (orderId <= 0) {
                throw new DatabaseException("Failed to generate order ID");
            }

            for (OrderItem it : items) {
                it.setOrderId(orderId);
            }
            orderItemDAO.createBatch(items, orderId, conn);

            conn.commit();
            order.setOrderId(orderId);
            order.setItems(items);
            LOGGER.info("Order successfully created: " + order.getOrderNumber());
            return order;
        } catch (Exception e) {
            DBConnection.rollback(conn);
            LOGGER.log(Level.SEVERE, "Failed to create order", e);
            throw new DatabaseException("Order creation failed: " + e.getMessage(), e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Maps packaging service ID to the associated packaging inventory material ID.
     */
    public int mapServiceToMaterialId(int serviceId) {
        return switch (serviceId) {
            case 1 -> 1; // Corrugated Box Packing -> Medium Box (MAT-BOX-M)
            case 2 -> 3; // Stand-Up Pouch Packing -> Barrier Pouch (MAT-POUCH-500)
            case 3 -> 5; // Barcode & Compliance Labeling -> Thermal Barcode Labels (MAT-LABEL-ROLL)
            case 4 -> 4; // Anti-Shock Bubble Cushioning -> Heavy Duty Bubble Roll (MAT-BUBBLE-ROLL)
            case 5 -> 1; // Multi-Product Kitting -> Master Box (MAT-BOX-M)
            case 6 -> 6; // Shrink Wrap / Sealing -> Reinforced Tape (MAT-TAPE-BRN)
            default -> 1; // Default fallback to primary carton
        };
    }

    /**
     * Approves an Order and Reserves Required Packaging Materials using an Atomic JDBC Transaction.
     * If inventory is insufficient, the entire operation rolls back and throws InsufficientInventoryException.
     *
     * @param orderId Target order ID
     * @throws InvalidStatusTransitionException if order is not in PENDING status
     * @throws InsufficientInventoryException if materials are not available
     */
    public void approveOrderAndReserveInventory(int orderId)
            throws InvalidStatusTransitionException, InsufficientInventoryException {

        Optional<Order> optOrder = getOrderById(orderId);
        if (optOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        Order order = optOrder.get();
        if (!order.getStatus().canTransitionTo(OrderStatus.APPROVED)) {
            throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.APPROVED.name());
        }

        // Calculate material demands per inventory item
        Map<Integer, Integer> materialDemands = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            int matId = mapServiceToMaterialId(item.getServiceId());
            int requiredUnits = item.getQuantity();
            // Roll items demand e.g. 1 roll per 25 items
            if (matId == 4 || matId == 5 || matId == 6) {
                requiredUnits = Math.max(1, (int) Math.ceil(requiredUnits / 25.0));
            }
            materialDemands.merge(matId, requiredUnits, Integer::sum);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin JDBC Transaction

            // 1. Check stock availability for all required materials
            for (Map.Entry<Integer, Integer> entry : materialDemands.entrySet()) {
                int inventoryId = entry.getKey();
                int required = entry.getValue();

                Optional<InventoryItem> optInv = inventoryDAO.findById(inventoryId, conn);
                if (optInv.isEmpty()) {
                    throw new InsufficientInventoryException("Packaging material ID " + inventoryId + " not found in database.");
                }

                InventoryItem item = optInv.get();
                if (item.getQuantityAvailable() < required) {
                    throw new InsufficientInventoryException(
                            item.getInventoryId(),
                            item.getMaterialName(),
                            required,
                            item.getQuantityAvailable()
                    );
                }
            }

            // 2. Reserve stock and log RESERVATION transactions
            for (Map.Entry<Integer, Integer> entry : materialDemands.entrySet()) {
                int inventoryId = entry.getKey();
                int required = entry.getValue();

                boolean reserved = inventoryDAO.reserveStock(inventoryId, required, conn);
                if (!reserved) {
                    throw new InsufficientInventoryException("Failed to reserve material ID: " + inventoryId);
                }

                InventoryTransaction tx = new InventoryTransaction();
                tx.setInventoryId(inventoryId);
                tx.setTransactionType(TransactionType.RESERVATION);
                tx.setQuantity(required);
                tx.setReferenceType("ORDER");
                tx.setReferenceId(orderId);
                inventoryTransactionDAO.create(tx, conn);
            }

            // 3. Update Order Status to APPROVED
            orderDAO.updateStatus(orderId, OrderStatus.APPROVED, conn);

            conn.commit(); // Commit JDBC Transaction
            LOGGER.info(String.format("Order %s successfully APPROVED. Inventory reserved.", order.getOrderNumber()));
        } catch (InsufficientInventoryException e) {
            DBConnection.rollback(conn);
            LOGGER.warning("Approval rejected due to insufficient inventory: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            DBConnection.rollback(conn);
            LOGGER.log(Level.SEVERE, "Unexpected error approving order ID: " + orderId, e);
            throw new DatabaseException("Failed to approve order: " + e.getMessage(), e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Starts packaging task and transitions order to PROCESSING.
     */
    public void startProcessing(int orderId, Integer assignedToUserId) throws InvalidStatusTransitionException {
        Optional<Order> opt = getOrderById(orderId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Order not found: " + orderId);

        Order order = opt.get();
        if (!order.getStatus().canTransitionTo(OrderStatus.PROCESSING)) {
            throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.PROCESSING.name());
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Update order status
            orderDAO.updateStatus(orderId, OrderStatus.PROCESSING, conn);

            // Create or update Packaging Task
            PackagingTask task = new PackagingTask();
            task.setOrderId(orderId);
            task.setAssignedTo(assignedToUserId);
            task.setQuantity(order.getTotalQuantity());
            task.setCompletedQuantity(0);
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setStartedAt(Timestamp.from(Instant.now()));
            packagingTaskDAO.create(task, conn);

            conn.commit();
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed to start processing order ID: " + orderId, e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Submits order to Quality Check inspection.
     */
    public void sendToQualityCheck(int orderId) throws InvalidStatusTransitionException {
        Optional<Order> opt = getOrderById(orderId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Order not found: " + orderId);

        Order order = opt.get();
        if (!order.getStatus().canTransitionTo(OrderStatus.QUALITY_CHECK)) {
            throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.QUALITY_CHECK.name());
        }

        orderDAO.updateStatus(orderId, OrderStatus.QUALITY_CHECK);
    }

    /**
     * Records Quality Inspection result. If PASSED, marks order as COMPLETED and consumes reserved inventory.
     */
    public void recordQualityCheck(int orderId, int inspectorUserId, int passedQty, int failedQty, String remarks, QualityStatus status)
            throws InvalidStatusTransitionException {

        Optional<Order> opt = getOrderById(orderId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Order not found: " + orderId);

        Order order = opt.get();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Record Quality Check
            QualityCheck qc = new QualityCheck();
            qc.setOrderId(orderId);
            qc.setCheckedBy(inspectorUserId);
            qc.setQuantityChecked(passedQty + failedQty);
            qc.setQuantityPassed(passedQty);
            qc.setQuantityFailed(failedQty);
            qc.setRemarks(remarks);
            qc.setStatus(status);
            qualityCheckDAO.create(qc, conn);

            // 2. If Passed, advance to COMPLETED and consume reserved inventory
            if (status == QualityStatus.PASSED) {
                if (!order.getStatus().canTransitionTo(OrderStatus.COMPLETED)) {
                    throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.COMPLETED.name());
                }

                orderDAO.updateStatus(orderId, OrderStatus.COMPLETED, conn);

                // Consume inventory that was reserved
                for (OrderItem item : order.getItems()) {
                    int matId = mapServiceToMaterialId(item.getServiceId());
                    int units = item.getQuantity();
                    if (matId == 4 || matId == 5 || matId == 6) {
                        units = Math.max(1, (int) Math.ceil(units / 25.0));
                    }
                    inventoryDAO.consumeStock(matId, units, conn);

                    InventoryTransaction tx = new InventoryTransaction();
                    tx.setInventoryId(matId);
                    tx.setTransactionType(TransactionType.CONSUMPTION);
                    tx.setQuantity(units);
                    tx.setReferenceType("ORDER");
                    tx.setReferenceId(orderId);
                    inventoryTransactionDAO.create(tx, conn);
                }
            }

            conn.commit();
        } catch (InvalidStatusTransitionException e) {
            DBConnection.rollback(conn);
            throw e;
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed recording quality check", e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Dispatches order with courier and tracking number.
     */
    public void dispatchOrder(int orderId, String deliveryPartner, String trackingNumber) throws InvalidStatusTransitionException {
        Optional<Order> opt = getOrderById(orderId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Order not found: " + orderId);

        Order order = opt.get();
        if (!order.getStatus().canTransitionTo(OrderStatus.DISPATCHED)) {
            throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.DISPATCHED.name());
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            orderDAO.updateStatus(orderId, OrderStatus.DISPATCHED, conn);

            DispatchRecord dispatch = new DispatchRecord();
            dispatch.setOrderId(orderId);
            dispatch.setDispatchDate(Date.valueOf(LocalDate.now()));
            dispatch.setDeliveryPartner(deliveryPartner);
            dispatch.setTrackingNumber(trackingNumber);
            dispatch.setStatus(DispatchStatus.IN_TRANSIT);
            dispatchDAO.create(dispatch, conn);

            conn.commit();
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed dispatching order ID: " + orderId, e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Marks order and dispatch as DELIVERED.
     */
    public void markOrderDelivered(int orderId) throws InvalidStatusTransitionException {
        Optional<Order> opt = getOrderById(orderId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Order not found: " + orderId);

        Order order = opt.get();
        if (!order.getStatus().canTransitionTo(OrderStatus.DELIVERED)) {
            throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.DELIVERED.name());
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            orderDAO.updateStatus(orderId, OrderStatus.DELIVERED, conn);
            dispatchDAO.findByOrderId(orderId).ifPresent(d -> {
                dispatchDAO.updateStatus(d.getDispatchId(), DispatchStatus.DELIVERED);
            });

            conn.commit();
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed delivering order ID: " + orderId, e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Cancels an order. If order had reserved inventory (APPROVED or PROCESSING), releases the reservation atomically.
     */
    public void cancelOrder(int orderId, String reason) throws InvalidStatusTransitionException {
        Optional<Order> opt = getOrderById(orderId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Order not found: " + orderId);

        Order order = opt.get();
        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidStatusTransitionException(order.getStatus().name(), OrderStatus.CANCELLED.name());
        }

        boolean hadReservation = order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.PROCESSING;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            orderDAO.updateStatus(orderId, OrderStatus.CANCELLED, conn);

            if (hadReservation) {
                // Release reserved inventory
                for (OrderItem item : order.getItems()) {
                    int matId = mapServiceToMaterialId(item.getServiceId());
                    int units = item.getQuantity();
                    if (matId == 4 || matId == 5 || matId == 6) {
                        units = Math.max(1, (int) Math.ceil(units / 25.0));
                    }
                    inventoryDAO.releaseStock(matId, units, conn);

                    InventoryTransaction tx = new InventoryTransaction();
                    tx.setInventoryId(matId);
                    tx.setTransactionType(TransactionType.RELEASE);
                    tx.setQuantity(units);
                    tx.setReferenceType("ORDER_CANCEL: " + (reason != null ? reason : ""));
                    tx.setReferenceId(orderId);
                    inventoryTransactionDAO.create(tx, conn);
                }
            }

            conn.commit();
            LOGGER.info("Order cancelled successfully: " + order.getOrderNumber());
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed cancelling order ID: " + orderId, e);
        } finally {
            DBConnection.close(conn);
        }
    }
}
