package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myforum.common.BusinessException;
import com.myforum.mapper.MessageMapper;
import com.myforum.mapper.UserMapper;
import com.myforum.model.dto.WsEvent;
import com.myforum.model.entity.Message;
import com.myforum.model.entity.Notification;
import com.myforum.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messaging;

    @Transactional
    public Message send(Long fromId, Long toId, String content) {
        if (content == null || content.isBlank()) throw new BusinessException("消息内容不能为空");
        if (fromId.equals(toId)) throw new BusinessException("不能给自己发私信");

        User toUser = userMapper.selectById(toId);
        if (toUser == null) throw new BusinessException("用户不存在");

        Message msg = new Message();
        msg.setFromId(fromId);
        msg.setToId(toId);
        msg.setContent(content.trim());
        msg.setIsRead(0);
        messageMapper.insert(msg);

        msg.setFromUser(userMapper.selectById(fromId));

        // 创建通知
        User fromUser = userMapper.selectById(fromId);
        notificationService.create(toId, fromId, Notification.TYPE_MESSAGE,
                (fromUser != null ? fromUser.getUsername() : "用户") + " 给你发了一条私信", msg.getId());

        // Push WebSocket event to recipient
        messaging.convertAndSend("/topic/message/" + toId,
                new WsEvent("new_message", msg));

        return msg;
    }

    public Map<String, Object> conversation(Long userId, Long withUserId, int page, int pageSize) {
        Page<Message> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .and(w -> w
                    .eq(Message::getFromId, userId).eq(Message::getToId, withUserId)
                    .or()
                    .eq(Message::getFromId, withUserId).eq(Message::getToId, userId))
                .orderByDesc(Message::getCreatedAt);
        Page<Message> result = messageMapper.selectPage(pageObj, wrapper);

        // Mark as read
        List<Message> msgs = result.getRecords();
        Collections.reverse(msgs); // oldest first
        for (Message m : msgs) {
            if (m.getToId().equals(userId) && m.getIsRead() == 0) {
                m.setIsRead(1);
                messageMapper.updateById(m);
            }
            m.setFromUser(userMapper.selectById(m.getFromId()));
        }

        long total = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .and(w -> w
                    .eq(Message::getFromId, userId).eq(Message::getToId, withUserId)
                    .or()
                    .eq(Message::getFromId, withUserId).eq(Message::getToId, userId)));

        return Map.of("list", msgs, "total", total, "page", page, "page_size", pageSize);
    }

    public Map<String, Object> conversations(Long userId) {
        // Get distinct users this user has messaged with
        List<Message> sent = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getFromId, userId)
                .orderByDesc(Message::getCreatedAt));
        List<Message> received = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getToId, userId)
                .orderByDesc(Message::getCreatedAt));

        Map<Long, Message> latestByUser = new LinkedHashMap<>();
        for (Message m : sent) {
            latestByUser.putIfAbsent(m.getToId(), m);
        }
        for (Message m : received) {
            if (!latestByUser.containsKey(m.getFromId())) {
                latestByUser.put(m.getFromId(), m);
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestByUser.entrySet()) {
            Long otherId = entry.getKey();
            Message lastMsg = entry.getValue();
            User other = userMapper.selectById(otherId);
            if (other == null) continue;

            long unread = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .eq(Message::getFromId, otherId)
                    .eq(Message::getToId, userId)
                    .eq(Message::getIsRead, 0));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("user", Map.of("id", other.getId(), "username", other.getUsername(), "avatar", other.getAvatar() != null ? other.getAvatar() : ""));
            item.put("last_content", lastMsg.getContent().length() > 50 ? lastMsg.getContent().substring(0, 50) + "..." : lastMsg.getContent());
            item.put("last_time", lastMsg.getCreatedAt() != null ? lastMsg.getCreatedAt().toString() : "");
            item.put("unread", unread);
            items.add(item);
        }

        return Map.of("list", items);
    }

    public long unreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getToId, userId)
                .eq(Message::getIsRead, 0));
    }
}
