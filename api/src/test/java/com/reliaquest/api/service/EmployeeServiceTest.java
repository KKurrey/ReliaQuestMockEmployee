package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.constants.ServiceConstants;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class EmployeeServiceTest {

    @Mock
    private EmployeeApiClient employeeApiClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private SetOperations<String, Object> setOps;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    void testGetAllEmployees_CacheHit() {
        Employee e1 = new Employee();
        e1.setId("1");
        Employee e2 = new Employee();
        e2.setId("2");

        when(setOps.members(ServiceConstants.EMPLOYEE_IDS_KEY)).thenReturn(Set.of("1", "2"));
        when(valueOps.get("1")).thenReturn(e1);
        when(valueOps.get("2")).thenReturn(e2);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        verify(employeeApiClient, never()).get(any(), any());
    }

    @Test
    void testGetAllEmployees_CacheMiss() {
        Employee e1 = new Employee();
        e1.setId("1");

        when(setOps.members(ServiceConstants.EMPLOYEE_IDS_KEY)).thenReturn(null);
        when(employeeApiClient.get(any(), any())).thenReturn(List.of(e1));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        verify(valueOps).set("1", e1);
        verify(setOps).add(ServiceConstants.EMPLOYEE_IDS_KEY, "1");
    }

    @Test
    void testGetEmployeeById_CacheHit() {
        Employee emp = new Employee();
        emp.setId("101");
        when(valueOps.get("101")).thenReturn(emp);

        Employee result = employeeService.getEmployeeById("101");
        assertEquals("101", result.getId());
        verify(employeeApiClient, never()).get(any(), any(), any());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(valueOps.get("404")).thenReturn(null);

        WebClientResponseException notFoundException = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(), "Not Found", HttpHeaders.EMPTY, null, null);

        when(employeeApiClient.get(eq("/{id}"), any(), eq("404"))).thenThrow(notFoundException);

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById("404"));
    }

    @Test
    void testGetHighestSalary() {
        Employee e1 = new Employee();
        e1.setEmployee_salary(5000);
        Employee e2 = new Employee();
        e2.setEmployee_salary(10000);

        when(setOps.members(any())).thenReturn(null);
        when(employeeApiClient.get(any(), any())).thenReturn(List.of(e1, e2));

        int result = employeeService.getHighestSalary();
        assertEquals(10000, result);
    }

    @Test
    void testTopTenHighestEarningEmployeeNames() {
        Employee e1 = new Employee();
        e1.setEmployee_name("Alice");
        e1.setEmployee_salary(5000);
        Employee e2 = new Employee();
        e2.setEmployee_name("Bob");
        e2.setEmployee_salary(10000);

        when(setOps.members(any())).thenReturn(null);
        when(employeeApiClient.get(any(), any())).thenReturn(List.of(e1, e2));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();
        assertEquals(List.of("Bob", "Alice"), result);
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeInput input = new CreateEmployeeInput();
        Employee created = new Employee();
        created.setId("201");

        when(employeeApiClient.post(any(), eq(input), any())).thenReturn(created);

        Employee result = employeeService.createEmployee(input);

        assertEquals("201", result.getId());
        verify(valueOps).set("201", created);
        verify(setOps).add(ServiceConstants.EMPLOYEE_IDS_KEY, "201");
    }

    @Test
    void testDeleteEmployeeById() {
        Employee emp = new Employee();
        emp.setId("301");
        emp.setEmployee_name("John");

        when(valueOps.get("301")).thenReturn(null);
        when(employeeApiClient.get(any(), any(), eq("301"))).thenReturn(emp);

        employeeService.deleteEmployeeById("301");

        verify(employeeApiClient).delete(any(), any(), any());
        verify(redisTemplate).delete("301");
        verify(setOps).remove(ServiceConstants.EMPLOYEE_IDS_KEY, "301");
    }
}
