package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.reliaquest.api.constants.ControllerConstants;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees() {
        List<Employee> employees = List.of(new Employee(), new Employee());
        when(employeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetEmployeesByNameSearch() {
        List<Employee> result = List.of(new Employee());
        when(employeeService.searchEmployeesByName("Alice")).thenReturn(result);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("Alice");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetEmployeeById() {
        Employee mockEmployee = new Employee();
        mockEmployee.setId("123");
        when(employeeService.getEmployeeById("123")).thenReturn(mockEmployee);

        ResponseEntity<Employee> response = employeeController.getEmployeeById("123");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("123", response.getBody().getId());
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        when(employeeService.getHighestSalary()).thenReturn(100000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(100000, response.getBody());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        List<String> topNames = List.of("Alice", "Bob");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topNames);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(List.of("Alice", "Bob"), response.getBody());
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("John");
        Employee created = new Employee();
        created.setEmployee_name("John");

        when(employeeService.createEmployee(input)).thenReturn(created);

        ResponseEntity<Employee> response = employeeController.createEmployee(input);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("John", response.getBody().getEmployee_name());
    }

    @Test
    void testDeleteEmployeeById() {
        doNothing().when(employeeService).deleteEmployeeById("123");

        ResponseEntity<String> response = employeeController.deleteEmployeeById("123");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ControllerConstants.EMPLOYEE_DELETED_SUCCESSFULLY, response.getBody());
    }
}
