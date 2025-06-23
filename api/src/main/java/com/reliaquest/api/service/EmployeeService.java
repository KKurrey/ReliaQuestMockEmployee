package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.constants.ExceptionConstants;
import com.reliaquest.api.constants.ServiceConstants;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.DeleteEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeApiClient employeeApiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<Employee> getAllEmployees() {

        Set<Object> idSet = redisTemplate.opsForSet().members(ServiceConstants.EMPLOYEE_IDS_KEY);
        List<String> ids = idSet != null ? idSet.stream().map(Object::toString).toList() : Collections.emptyList();

        List<Employee> cachedEmployees = ids.stream()
                .map(id -> (Employee) redisTemplate.opsForValue().get(id))
                .filter(Objects::nonNull)
                .toList();

        if (!cachedEmployees.isEmpty() && cachedEmployees.size() == ids.size()) {
            log.info("Fetched {} employees from cache", cachedEmployees.size());
            return cachedEmployees;
        }

        // Fetch from API and refresh cache
        log.info("Cache miss or incomplete. Fetching employees from API...");
        List<Employee> employees = employeeApiClient.get("", new ParameterizedTypeReference<>() {});

        if (employees == null) {
            log.warn("Received null employee list from API");
            return Collections.emptyList();
        }

        redisTemplate.delete(ServiceConstants.EMPLOYEE_IDS_KEY);
        for (String id : ids) {
            redisTemplate.delete(id);
        }
        for (Employee employee : employees) {
            if (employee != null && employee.getId() != null) {
                redisTemplate.opsForValue().set(employee.getId(), employee);
                redisTemplate.opsForSet().add(ServiceConstants.EMPLOYEE_IDS_KEY, employee.getId());
                log.debug("Cached employee ID {}", employee.getId());
            } else {
                log.warn("Skipping employee with null or missing ID: {}", employee);
            }
        }

        log.info("Cached {} employees from API", employees.size());

        return employees;
    }

    public List<Employee> searchEmployeesByName(String searchString) {

        List<Employee> employeeList = getAllEmployees();

        if (searchString == null || searchString.isEmpty()) {
            return employeeList;
        }

        List<Employee> result = employeeList.stream()
                .filter(Objects::nonNull)
                .filter(e -> {
                    String name = e.getEmployee_name();
                    return name != null && name.contains(searchString);
                })
                .toList();

        log.info("Found {} employees matching name '{}'", result.size(), searchString);

        return result;
    }

    public Employee getEmployeeById(String id) {

        Employee cachedEmployee = (Employee) redisTemplate.opsForValue().get(id);

        if (cachedEmployee != null) {
            log.info("Fetched employee with ID {} from cache", id);
            return cachedEmployee;
        }

        try {

            log.info("Cache miss. Fetching employee with ID {} from API...", id);

            Employee emp = employeeApiClient.get("/{id}", new ParameterizedTypeReference<>() {}, id);
            if (emp == null || emp.getId() == null) {
                throw new EmployeeNotFoundException(String.format(ExceptionConstants.EXC_EMPLOYEE_NOT_FOUND, id));
            }

            redisTemplate.opsForValue().set(emp.getId(), emp);
            redisTemplate.opsForSet().add(ServiceConstants.EMPLOYEE_IDS_KEY, emp.getId());
            log.info("Employee with ID {} fetched and cached", emp.getId());

            return emp;

        } catch (WebClientResponseException.NotFound ex) {
            log.warn("Employee with ID {} not found in API", id);
            throw new EmployeeNotFoundException(String.format(ExceptionConstants.EXC_EMPLOYEE_NOT_FOUND, id));
        }
    }

    public Integer getHighestSalary() {

        List<Employee> employees = getAllEmployees();
        if (employees == null || employees.isEmpty()) {
            log.warn("No employees available for salary computation");
            throw new IllegalStateException(ExceptionConstants.EXC_NO_EMPLOYEES_AVAILABLE);
        }

        Integer maxSalary = employees.stream()
                .filter(e -> e != null && e.getEmployee_salary() != null)
                .map(Employee::getEmployee_salary)
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException(ExceptionConstants.EXC_NO_VALID_SALARIES_FOUND));

        log.info("Highest salary among employees: {}", maxSalary);

        return maxSalary;
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {

        List<Employee> employees = getAllEmployees();
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> topTenEarners = employees.stream()
                .filter(e -> e != null && e.getEmployee_salary() != null && e.getEmployee_name() != null)
                .sorted(Comparator.comparingInt(Employee::getEmployee_salary).reversed())
                .limit(10)
                .map(Employee::getEmployee_name)
                .toList();

        log.info("Top 10 highest earning employees: {}", topTenEarners);

        return topTenEarners;
    }

    public Employee createEmployee(CreateEmployeeInput input) {

        Employee created = employeeApiClient.post("", input, new ParameterizedTypeReference<>() {});
        if (created == null || created.getId() == null) {
            log.error("Failed to create employee with input: {}", input);
            throw new IllegalStateException(ExceptionConstants.EXC_CREATE_EMPLOYEE_FAILED);
        }

        redisTemplate.opsForValue().set(created.getId(), created);
        redisTemplate.opsForSet().add(ServiceConstants.EMPLOYEE_IDS_KEY, created.getId());

        log.info("Employee created with ID: {}", created.getId());

        return created;
    }

    public void deleteEmployeeById(String id) {

        Employee employee = getEmployeeById(id);
        if (employee == null || employee.getEmployee_name() == null) {
            log.error("Cannot delete employee: ID {} is invalid", id);
            throw new IllegalStateException(ExceptionConstants.EXC_CANNOT_DELETE_NULL_EMPLOYEE);
        }

        DeleteEmployeeInput input = new DeleteEmployeeInput();
        input.setName(employee.getEmployee_name());

        employeeApiClient.delete("", input, new ParameterizedTypeReference<>() {});
        redisTemplate.delete(id);
        redisTemplate.opsForSet().remove(ServiceConstants.EMPLOYEE_IDS_KEY, id);

        log.info("Employee with ID {} deleted and removed from cache", id);
    }
}
