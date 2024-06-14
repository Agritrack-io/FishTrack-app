package io.agritrack.philosofish.data.dao.common;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.common.Employee;

@Dao
public interface EmployeeDAO {

    @Query("SELECT * from employee")
    List<Employee> getAll();


    @Query("SELECT * from employee where site=:siteName")
    List<Employee> getBySite(String siteName);

    @Query("SELECT * from employee where id=:employeeId LIMIT 1")
    Employee getById(Long employeeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Employee... employees);

    @Delete
    void delete(Employee employee);

    @Query("DELETE from employee")
    void deleteAll();

    @Update
    void update(Employee employee);
}
