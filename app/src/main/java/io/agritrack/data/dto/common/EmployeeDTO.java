package io.agritrack.data.dto.common;

import io.agritrack.data.model.common.Employee;

public class EmployeeDTO {

    public Long id;
    public Short order;
    public String email;
    public String first_name;
    public String last_name;
    public String phone;
    public Boolean enabled;
    public Long registered_at;
    public String role_description;
    public Long supervisor;
    public Long site;
    public Long user;

    public static Employee convert(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.id = employeeDTO.id;
        employee.order = employeeDTO.order;
        employee.email = employeeDTO.email;
        employee.firstName = employeeDTO.first_name;
        employee.lastName = employeeDTO.last_name;
        employee.phone = employeeDTO.phone;
        employee.enabled = employeeDTO.enabled;
        employee.registeredAt = employeeDTO.registered_at;
        employee.roleDescription = employeeDTO.role_description;
        employee.supervisor = employeeDTO.supervisor;
        employee.site = employeeDTO.site;
        employee.user = employeeDTO.user;
        return employee;
    }
}
