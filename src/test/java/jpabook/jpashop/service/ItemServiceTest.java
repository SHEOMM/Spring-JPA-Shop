package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.*;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ItemServiceTest {
    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Test
    public void 아이템_등록() throws Exception{
        Item item = new Item();
        item.setName("삼겹살");

        itemService.saveItem(item);

        assertEquals(item, itemRepository.findOne(item.getId()));
    }



}