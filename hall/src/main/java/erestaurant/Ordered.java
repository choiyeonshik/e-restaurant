package erestaurant;

public class Ordered extends AbstractEvent {

    private Long orderid;
    private Long employeeCardNo;
    private String menuname;
    private Long amount;

    public Ordered(){
        super();
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
    public Long getEmployeeCardNo() {
        return employeeCardNo;
    }

    public void setEmployeeCardNo(Long employeeCardNo) {
        this.employeeCardNo = employeeCardNo;
    }
    public String getMenuname() {
        return menuname;
    }

    public void setMenuname(String menuname) {
        this.menuname = menuname;
    }
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
