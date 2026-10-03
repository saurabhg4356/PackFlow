package com.packflow.dao.impl;

import com.packflow.dao.PackagingServiceDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.PackagingServiceItem;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackagingServiceDAOImpl implements PackagingServiceDAO {

    private PackagingServiceItem mapRow(ResultSet rs) throws SQLException {
        return new PackagingServiceItem(
                rs.getInt("service_id"),
                rs.getString("service_name"),
                rs.getString("description"),
                rs.getBigDecimal("base_price"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }

    @Override
    public Optional<PackagingServiceItem> findById(int serviceId) {
        String sql = "SELECT * FROM packaging_services WHERE service_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding packaging service by ID: " + serviceId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<PackagingServiceItem> findAll() {
        List<PackagingServiceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM packaging_services ORDER BY service_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all packaging services", e);
        }
        return list;
    }

    @Override
    public List<PackagingServiceItem> findActiveServices() {
        List<PackagingServiceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM packaging_services WHERE status = 'ACTIVE' ORDER BY service_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching active packaging services", e);
        }
        return list;
    }

    @Override
    public boolean create(PackagingServiceItem service) {
        String sql = "INSERT INTO packaging_services (service_name, description, base_price, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getBasePrice());
            stmt.setString(4, service.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        service.setServiceId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating packaging service: " + service.getServiceName(), e);
        }
    }

    @Override
    public boolean update(PackagingServiceItem service) {
        String sql = "UPDATE packaging_services SET service_name = ?, description = ?, base_price = ?, status = ? WHERE service_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getBasePrice());
            stmt.setString(4, service.getStatus());
            stmt.setInt(5, service.getServiceId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating packaging service ID: " + service.getServiceId(), e);
        }
    }

    @Override
    public boolean delete(int serviceId) {
        String sql = "DELETE FROM packaging_services WHERE service_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting packaging service ID: " + serviceId, e);
        }
    }
}
