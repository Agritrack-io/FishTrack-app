package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "employee")
public class Employee {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "hierarchy_order")
    private Short order;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "enabled")
    private Boolean enabled;

    @ColumnInfo(name = "registered_at")
    private Long registeredAt;

    @ColumnInfo(name = "role_description")
    private String roleDescription;

    @ColumnInfo(name = "supervisor")
    private Long supervisor;

   /* @ManyToOne
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Employee_Site"))
    private Site site;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Employee_User"))
    private User user;*/
}
