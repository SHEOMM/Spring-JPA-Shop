package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }
    // 동적 쿼리를 만드는 게 어렵다.
    // 첫번째 무식한 방법. - jpql 그대로 쓰기
    public List<Order> findAllByString(OrderSearch orderSearch){

        String jpql = "select o from Order o join o.member m";

        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();

    }

    /**
     * JPA Criteria
     * 치명적인 단점이 있다 : 유지보수성이 제로에 가깝다.
     * 아래 코드를 보고 모든 JPQL을 만드는지 눈에 들어오는가.
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName()
                            + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000

        return query.getResultList();
    }


    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order  o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order  o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    // distinct가 없으면 order - orderItems가 일대 다이기 때문에 order가 중복되어서 나온다.
    // distinct의 경우 DB에 일단 쿼리로도 나간다.
    // 하지만 distinct는 row가 동일해야되는데 여긴 아니므로 DB 상으론 불가능하다.
    // application에 가져와서 id가 똑같으면 구별해서 버려주므로 JPA 단에서 가능해진다.
    // 즉 distinct는 DB에서 한번, application 단에서 한번 더 검증해준다.
    // 문제는 fetch join을 쓰게 되면 paging을 db 상에서가 아니라 memory 단에 가져와서 처리하기 시작한다.
    // 그럼 데이터가 많으면? 터져버린다.
    // 왜 이렇게 하는가? DB Query에서는 데이터가 4개이다. 일대다 join을 하는 순간 order의 기준이 흩어져버린다.
    // 우리가 원하는 결과가 안 나온다. 그래서 DB 상에서 작업이 불가능해진다.
    // 위에서 말한 것처럼 distinct도 application 단에서 가져와서 작업도 하기 때문이다.
    // 쓰면 큰일난다.
    // 컬렉션 페치 조인은 딱 하나만 사용해야 한다.
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o"+
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch  oi.item i", Order.class
                ).getResultList();
    }
}
