package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
public class Address {
    public Address() {
    }

    private String city;
    private String street;

    private String zipcode;
}
