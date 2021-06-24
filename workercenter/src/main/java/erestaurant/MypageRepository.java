package erestaurant;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MypageRepository extends CrudRepository<Mypage, Long> {

    List<Mypage> findByOrderid(Long orderid);
    List<Mypage> findByCookid(Long cookid);
    List<Mypage> findByPaymentid(Long paymentid);

}