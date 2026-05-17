package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myforum.mapper.NotificationMapper;
import com.myforum.mapper.UserMapper;
import com.myforum.model.dto.WsEvent;
import com.myforum.model.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messaging;

    @Transactional
    public void create(Long userId, Long fromId, Integer type, String content, Long targetId) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setFromId(fromId);
        notif.setType(type);
        notif.setContent(content);
        notif.setTargetId(targetId);
        notif.setIsRead(0);
        notificationMapper.insert(notif);

        // Push WebSocket to recipient
        messaging.convertAndSend("/topic/user/" + userId,
                new WsEvent("new_notification", notif));
    }

    public Map<String, Object> list(Long userId, int page, int pageSize) {
        Page<Notification> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(pageObj, wrapper);

        Long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));

        for (Notification n : result.getRecords()) {
            if (n.getFromId() > 0) n.setFromUser(userMapper.selectById(n.getFromId()));
        }

        return Map.of(
                "list", result.getRecords(),
                "total", result.getTotal(),
                "unread_count", unreadCount,
                "page", page,
                "page_size", pageSize
        );
    }

    public long countUnread(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification notif = notificationMapper.selectById(id);
        if (notif != null && notif.getUserId().equals(userId)) {
            notif.setIsRead(1);
            notificationMapper.updateById(notif);
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        Notification update = new Notification();
        update.setIsRead(1);
        notificationMapper.update(update, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0));
    }
}
