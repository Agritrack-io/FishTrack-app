package io.agritrack.fishtrack.data.dto.common;

import io.agritrack.fishtrack.data.model.common.Employee;

public class EmployeeDTO {

    public Long id;
    public Short order;
    public String email;
    public String firstName;
    public String lastName;
    public String phone;
    public Boolean enabled;
    public Long registeredAt;
    public String roleDescription;
    public Long supervisor;

    public static Employee convert(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.id = employeeDTO.id;
        employee.order = employeeDTO.order;
        employee.email = employeeDTO.email;
        employee.firstName = employeeDTO.firstName;
        employee.lastName = employeeDTO.lastName;
        employee.phone = employeeDTO.phone;
        employee.enabled = employeeDTO.enabled;
        employee.registeredAt = employeeDTO.registeredAt;
        employee.roleDescription = employeeDTO.roleDescription;
        employee.supervisor = employeeDTO.supervisor;
        return employee;
    }
}
