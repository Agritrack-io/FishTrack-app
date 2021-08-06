package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "employee")
public class Employee {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "hierarchy_order")
    public Short order;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "enabled")
    public Boolean enabled;

    @ColumnInfo(name = "registered_at")
    public Long registeredAt;

    @ColumnInfo(name = "role_description")
    public String roleDescription;

    @ColumnInfo(name = "supervisor")
    public Long supervisor;

   /* @ManyToOne
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Employee_Site"))
    public Site site;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Employee_User"))
    public User user;*/
}
