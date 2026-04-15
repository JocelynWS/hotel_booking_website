package com.hotel.repository;

import com.hotel.model.Employee;
import com.hotel.model.EmployeeRole;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository extends JsonRepository<Employee, String> {

    private static final Type LIST_TYPE = new TypeToken<List<Employee>>(){}.getType();

    public EmployeeRepository() {
        super("employees.json", LIST_TYPE);
    }

    @Override
    protected String getId(Employee entity) {
        return entity.getEmployeeId();
    }

    public List<Employee> findByRole(EmployeeRole role) {
        return cache.stream()
                .filter(e -> e.getRole() == role)
                .toList();
    }

    public List<Employee> findActive() {
        return cache.stream()
                .filter(Employee::isActive)
                .toList();
    }

    public Optional<Employee> findByEmail(String email) {
        return cache.stream()
                .filter(e -> e.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
