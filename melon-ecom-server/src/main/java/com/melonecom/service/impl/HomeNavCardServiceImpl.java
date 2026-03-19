package com.melonecom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.HomeNavCardItemMapper;
import com.melonecom.mapper.HomeNavCardMapper;
import com.melonecom.model.dto.HomeNavCardSaveDTO;
import com.melonecom.model.entity.HomeNavCard;
import com.melonecom.model.entity.HomeNavCardItem;
import com.melonecom.model.vo.HomeNavCardVO;
import com.melonecom.result.Result;
import com.melonecom.service.IHomeNavCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HomeNavCardServiceImpl extends ServiceImpl<HomeNavCardMapper, HomeNavCard> implements IHomeNavCardService {

    private static final int REQUIRED_CARD_COUNT = 4;
    private static final int REQUIRED_ITEM_COUNT = 4;

    private final HomeNavCardMapper homeNavCardMapper;
    private final HomeNavCardItemMapper homeNavCardItemMapper;

    public HomeNavCardServiceImpl(HomeNavCardMapper homeNavCardMapper,
                                  HomeNavCardItemMapper homeNavCardItemMapper) {
        this.homeNavCardMapper = homeNavCardMapper;
        this.homeNavCardItemMapper = homeNavCardItemMapper;
    }

    @Override
    public Result<List<HomeNavCardVO>> getAdminConfig() {
        return Result.success(buildConfig());
    }

    @Override
    public Result<List<HomeNavCardVO>> getPublicConfig() {
        return Result.success(buildConfig());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> saveConfig(HomeNavCardSaveDTO dto) {
        if (dto == null || dto.getCards() == null || dto.getCards().size() != REQUIRED_CARD_COUNT) {
            return Result.error("首页导航卡必须固定保存 4 张");
        }

        for (int cardIndex = 0; cardIndex < dto.getCards().size(); cardIndex++) {
            HomeNavCardSaveDTO.CardConfig card = dto.getCards().get(cardIndex);
            if (!StringUtils.hasText(trimToNull(card.getTitle()))) {
                return Result.error("第 " + (cardIndex + 1) + " 张导航卡标题不能为空");
            }
            if (card.getItems() == null || card.getItems().size() != REQUIRED_ITEM_COUNT) {
                return Result.error("第 " + (cardIndex + 1) + " 张导航卡必须固定保存 4 个商品位");
            }

            for (int itemIndex = 0; itemIndex < card.getItems().size(); itemIndex++) {
                HomeNavCardSaveDTO.ItemConfig item = card.getItems().get(itemIndex);
                if (!StringUtils.hasText(trimToNull(item.getKeyword()))) {
                    return Result.error("第 " + (cardIndex + 1) + " 张导航卡第 " + (itemIndex + 1) + " 个商品位关键词不能为空");
                }

                if (!StringUtils.hasText(trimToNull(item.getTitle()))) {
                    return Result.error("第 " + (cardIndex + 1) + " 张导航卡第 " + (itemIndex + 1) + " 个商品位标题不能为空");
                }
                if (!StringUtils.hasText(trimToNull(item.getImageUrl()))) {
                    return Result.error("第 " + (cardIndex + 1) + " 张导航卡第 " + (itemIndex + 1) + " 个商品位图片不能为空");
                }
            }
        }

        homeNavCardItemMapper.delete(null);
        homeNavCardMapper.delete(null);

        for (int cardIndex = 0; cardIndex < dto.getCards().size(); cardIndex++) {
            HomeNavCardSaveDTO.CardConfig card = dto.getCards().get(cardIndex);

            HomeNavCard cardEntity = new HomeNavCard();
            cardEntity.setTitle(trimToNull(card.getTitle()));
            cardEntity.setSort(cardIndex + 1);
            homeNavCardMapper.insert(cardEntity);

            for (int itemIndex = 0; itemIndex < card.getItems().size(); itemIndex++) {
                HomeNavCardSaveDTO.ItemConfig item = card.getItems().get(itemIndex);

                HomeNavCardItem itemEntity = new HomeNavCardItem();
                itemEntity.setCardId(cardEntity.getCardId());
                itemEntity.setKeyword(trimToNull(item.getKeyword()));
                itemEntity.setTitle(trimToNull(item.getTitle()));
                itemEntity.setImageUrl(trimToNull(item.getImageUrl()));
                itemEntity.setSort(itemIndex + 1);
                homeNavCardItemMapper.insert(itemEntity);
            }
        }

        return Result.success("首页导航卡保存成功");
    }

    private List<HomeNavCardVO> buildConfig() {
        List<HomeNavCard> cards = homeNavCardMapper.selectList(
                new LambdaQueryWrapper<HomeNavCard>()
                        .orderByAsc(HomeNavCard::getSort)
                        .orderByAsc(HomeNavCard::getCardId)
        );

        if (cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> cardIds = cards.stream()
                .map(HomeNavCard::getCardId)
                .filter(Objects::nonNull)
                .toList();

        List<HomeNavCardItem> items = homeNavCardItemMapper.selectList(
                new LambdaQueryWrapper<HomeNavCardItem>()
                        .in(HomeNavCardItem::getCardId, cardIds)
                        .orderByAsc(HomeNavCardItem::getSort)
                        .orderByAsc(HomeNavCardItem::getItemId)
        );

        Map<Long, List<HomeNavCardItem>> itemMap = items.stream()
                .collect(Collectors.groupingBy(HomeNavCardItem::getCardId, LinkedHashMap::new, Collectors.toList()));

        List<HomeNavCardVO> result = new ArrayList<>();
        for (HomeNavCard card : cards) {
            HomeNavCardVO vo = new HomeNavCardVO();
            vo.setCardId(card.getCardId());
            vo.setTitle(card.getTitle());
            vo.setSort(card.getSort());

            List<HomeNavCardVO.ItemVO> itemVos = new ArrayList<>();
            for (HomeNavCardItem item : itemMap.getOrDefault(card.getCardId(), Collections.emptyList())) {
                HomeNavCardVO.ItemVO itemVO = new HomeNavCardVO.ItemVO();
                itemVO.setItemId(item.getItemId());
                itemVO.setKeyword(item.getKeyword());
                itemVO.setTitle(item.getTitle());
                itemVO.setImageUrl(item.getImageUrl());
                itemVO.setSort(item.getSort());
                itemVos.add(itemVO);
            }

            vo.setItems(itemVos);
            result.add(vo);
        }

        return result;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
