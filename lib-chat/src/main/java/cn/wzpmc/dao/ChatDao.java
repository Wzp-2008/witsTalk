package cn.wzpmc.dao;

import cn.wzpmc.pojo.HeadPortrait;
import cn.wzpmc.pojo.Message;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

/**
 * @Author wzp
 * @Date 2022/1/26
 * @Version 1.0
 */
public interface ChatDao {
    /**
     * send a message to database
     * @param message the message you want to send
     */
    void sendMessage(Message message);

    /**
     * get message with id > id_min and id < id_max
     * @param idMax the min value of id
     * @param idMin the max value of id
     * @return some message objects
     */
    ArrayList<Message> getMessage(@Param("id_min") int idMin, @Param("id_max") int idMax);

    /**
     * get all messages count
     * @return count of message
     */
    Integer getCount();

    /**
     * get a user's head portrait with username
     * @param username username
     * @return a base64 of user's head portrait
     */
    String getUserHeadPortrait(@Param("username") String username);

    /**
     * recall a message
     * @param message a message object
     */
    void recall(Message message);
}
