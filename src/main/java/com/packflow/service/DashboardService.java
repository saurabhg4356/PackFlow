package com.packflow.service;

import com.packflow.dao.DashboardDAO;
import com.packflow.dao.impl.DashboardDAOImpl;
import com.packflow.model.DashboardStats;

import java.sql.Date;

/**
 * Service providing live database metrics, real chart points, and KPIs for dashboards.
 * GUARANTEES NO FAKE DATA - all calculations delegated directly to MySQL SQL aggregations.
 */
public class DashboardService {

    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAOImpl();
    }

    public DashboardService(DashboardDAO dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    /**
     * Retrieves comprehensive dashboard analytics using live MySQL queries.
     *
     * @param dateFilter Filter string: 'all', 'today', 'this_week', 'this_month', 'this_year', 'custom'
     * @param startDate Custom start date (if filter is custom)
     * @param endDate Custom end date (if filter is custom)
     * @return Fully populated DashboardStats DTO
     */
    public DashboardStats getDashboardStatistics(String dateFilter, Date startDate, Date endDate) {
        return dashboardDAO.getComprehensiveStats(dateFilter, startDate, endDate);
    }
}
