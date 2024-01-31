package jpabook.jpashop.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import jpabook.jpashop.domain.item.Item;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Test
    @Transactional
    public void testItem() throws Exception{
        Item item = new Item();
        item.setName("삼겹살");

        Long saveId = itemRepository.save(item);
        Item findItem = itemRepository.findOne(saveId);

        assertThat(findItem.getId()).isEqualTo(item.getId());
    }

}