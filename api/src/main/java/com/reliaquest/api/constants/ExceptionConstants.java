package com.reliaquest.api.constants;

public class ExceptionConstants {

    public static final String EXC_TOO_MANY_REQUESTS_MESSAGE = "Too many requests. Please wait a moment and try again.";
    public static final String EXC_NO_EMPLOYEES_AVAILABLE = "No employees available";
    public static final String EXC_NO_VALID_SALARIES_FOUND = "No valid salaries found";
    public static final String EXC_EMPLOYEE_NOT_FOUND = "Employee with ID %s not found or has null ID";
    public static final String EXC_CANNOT_DELETE_NULL_EMPLOYEE = "Cannot delete: employee ID is invalid or NULL";
    public static final String EXC_CREATE_EMPLOYEE_FAILED = "Failed to create employee";
    public static final String EXC_REDIS_CONNECTION_FACTORY_NULL = "RedisConnectionFactory must not be null";
    public static final String EXC_EMPLOYEE_API_BASE_URL_NULL =
            "Employee API base URL is not configured. Please check your application properties.";
}
