package com.packflow.controller;

import com.packflow.model.InventoryItem;
import com.packflow.model.InventoryTransaction;
import com.packflow.model.Role;
import com.packflow.model.TransactionType;
import com.packflow.model.User;
import com.packflow.service.InventoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller managing packaging materials inventory, restocks, adjustments, and transactions.
 */
@WebServlet(name = "InventoryServlet", urlPatterns = {"/inventory"})
public class InventoryServlet extends HttpServlet {

    private InventoryService inventoryService;

    @Override
    public void init() {
        this.inventoryService = new InventoryService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("transactions".equalsIgnoreCase(action)) {
            listTransactions(req, resp);
            return;
        }

        listInventory(req, resp);
    }

    private void listInventory(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = 1;
        int pageSize = 10;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        String search = req.getParameter("search");
        String stockFilter = req.getParameter("stockFilter"); // 'HEALTHY', 'LOW', 'OUT'

        List<InventoryItem> items = inventoryService.getPaginatedInventory(page, pageSize, search, stockFilter);
        int totalItems = inventoryService.getTotalInventoryCount(search, stockFilter);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        req.setAttribute("items", items);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("search", search);
        req.setAttribute("stockFilter", stockFilter);

        String msg = req.getParameter("msg");
        if ("saved".equals(msg)) req.setAttribute("successMessage", "Inventory material saved successfully.");
        else if ("restocked".equals(msg)) req.setAttribute("successMessage", "Stock successfully replenished and purchase logged.");
        else if ("adjusted".equals(msg)) req.setAttribute("successMessage", "Stock adjustment recorded with audit log.");
        else if ("deleted".equals(msg)) req.setAttribute("successMessage", "Inventory item deleted.");

        req.getRequestDispatcher("/inventory.jsp").forward(req, resp);
    }

    private void listTransactions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = 1;
        int pageSize = 15;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        Integer invId = null;
        String invStr = req.getParameter("inventoryId");
        if (invStr != null && !invStr.isEmpty()) {
            try {
                invId = Integer.parseInt(invStr);
            } catch (NumberFormatException ignored) {
            }
        }

        String typeStr = req.getParameter("type");
        TransactionType type = (typeStr != null && !typeStr.isEmpty()) ? TransactionType.fromString(typeStr) : null;

        List<InventoryTransaction> transactions = inventoryService.getPaginatedTransactions(page, pageSize, invId, type);
        int total = inventoryService.getTotalTransactionCount(invId, type);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("transactions", transactions);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalTransactions", total);
        req.setAttribute("selectedInvId", invId);
        req.setAttribute("selectedType", type != null ? type.name() : "");
        req.setAttribute("allInventory", inventoryService.getAllInventory());

        req.getRequestDispatcher("/inventory-transactions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/inventory");
            return;
        }

        switch (action) {
            case "save" -> handleSaveMaterial(req, resp);
            case "restock" -> handleRestock(req, resp);
            case "adjust" -> handleAdjust(req, resp);
            case "delete" -> handleDelete(req, resp);
            default -> resp.sendRedirect(req.getContextPath() + "/inventory");
        }
    }

    private void handleSaveMaterial(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int inventoryId = 0;
            String idStr = req.getParameter("inventoryId");
            if (idStr != null && !idStr.isEmpty()) inventoryId = Integer.parseInt(idStr);

            InventoryItem item = new InventoryItem();
            item.setInventoryId(inventoryId);
            item.setMaterialName(req.getParameter("materialName"));
            item.setMaterialCode(req.getParameter("materialCode"));
            item.setQuantityAvailable(Integer.parseInt(req.getParameter("quantityAvailable")));
            item.setReorderLevel(Integer.parseInt(req.getParameter("reorderLevel")));
            item.setUnit(req.getParameter("unit"));

            inventoryService.saveInventory(item);
            resp.sendRedirect(req.getContextPath() + "/inventory?msg=saved");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/inventory?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleRestock(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int inventoryId = Integer.parseInt(req.getParameter("inventoryId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            String reference = req.getParameter("reference");

            inventoryService.restockMaterial(inventoryId, quantity, reference, null);
            resp.sendRedirect(req.getContextPath() + "/inventory?msg=restocked");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/inventory?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleAdjust(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int inventoryId = Integer.parseInt(req.getParameter("inventoryId"));
            int newAvailable = Integer.parseInt(req.getParameter("newAvailable"));
            String reason = req.getParameter("reason");

            inventoryService.adjustStock(inventoryId, newAvailable, reason);
            resp.sendRedirect(req.getContextPath() + "/inventory?msg=adjusted");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/inventory?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int inventoryId = Integer.parseInt(req.getParameter("inventoryId"));
            inventoryService.deleteInventory(inventoryId);
            resp.sendRedirect(req.getContextPath() + "/inventory?msg=deleted");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/inventory?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
