package top.xinsin.services;

import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xinsin.dao.UserMapper;
import top.xinsin.enums.HttpCodes;
import top.xinsin.pojo.User;
import top.xinsin.utils.JwtTokenUtils;
import top.xinsin.utils.ResultData;
import top.xinsin.utils.SqlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wzp
 * Created On 2021/12/11 21:21
 * @version 1.0
 */
@Service
@Slf4j
public class UserService {
    private final UserMapper userMapper;
    @Autowired
    public UserService(UserMapper userMapper){
        this.userMapper = userMapper;
    }
    public ResultData<JSONObject> canLogin(User user, HttpServletResponse response){
        log.info("canLogin args:user=" + user);
        JSONObject jsonObject = new JSONObject();
        String password = user.getPassword();
        String sha512Hex = DigestUtils.sha512Hex(password);
        user.setPassword(sha512Hex);
        User user1 = userMapper.canLogin(user);
        if (user1 != null){
            Map<String,String> payload = new HashMap<>(10);
            payload.put("id",String.valueOf(user1.getId()));
            payload.put("username",user1.getUsername());
            String token = JwtTokenUtils.getToken(payload);
            response.setHeader("token", token);
            response.setHeader("Access-Control-Expose-Headers", "token");
            jsonObject.fluentPut("base64", user1.getBase64())
                    .fluentPut("canLogin", true);
        } else {
            jsonObject.fluentPut("canLogin", false);
            return ResultData.failed(HttpCodes.HTTP_CODES501, jsonObject);
        }
        return ResultData.success(jsonObject);
    }

    public ResultData<JSONObject> addUser(User user) {
        log.info("addUser args:user=" + user);
        user.setPassword(DigestUtils.sha512Hex(user.getPassword()));
        JSONObject jsonObject = SqlUtils.insertOperate(userMapper.addUser(user));
        return ResultData.success(jsonObject);
    }

    public ResultData<JSONObject> changePassword(User user, HttpServletRequest request) {
        String token = request.getHeader("Access-Token");
        JSONObject jsonObject = new JSONObject();
        jsonObject.fluentPut("is_success", false);
        if (JwtTokenUtils.isUser(token, user.getUsername())) {
            String s = DigestUtils.sha512Hex(user.getPassword());
            user.setPassword(s);
            jsonObject = SqlUtils.updateOperate(userMapper.changePassword(user));
            jsonObject.fluentPut("is_success", true);
            return ResultData.success(jsonObject);
        }
        return ResultData.failed(HttpCodes.INVALID_TOKEN, jsonObject);
    }

    public void setOnline(User user) {
        SqlUtils.updateOperate(userMapper.setOnline(user));
        user.setOnline(1);
    }

    public void setOffline(User user) {
        SqlUtils.updateOperate(userMapper.setOffline(user));
        user.setOnline(0);
    }

    public ResultData<JSONObject> setHeadPortrait(User user, HttpServletRequest request) {
        // 因为传入两个string所以password即为base64
        String token = request.getHeader("Access-Token");
        JSONObject jsonObject = new JSONObject();
        jsonObject.fluentPut("is_success", false);
        if (JwtTokenUtils.isUser(token, user.getUsername())) {
            userMapper.setHeadPortrait(user);
            jsonObject.fluentPut("is_success", true);
            return ResultData.success(jsonObject);
        }
        return ResultData.failed(HttpCodes.INVALID_TOKEN, jsonObject);
    }

    public ResultData<String> getUserHeadPortrait(String name) {
        String b64 = userMapper.getUserHeadPortrait(name);
        return ResultData.success(b64);
    }

    public ResultData<List<User>> getOnlineUser() {
        return ResultData.success(userMapper.getOnlineUser());
    }

    public ResultData<Boolean> changeUsername(User user, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String token = request.getHeader("Access-Token");
        DecodedJWT tokenInfo = JwtTokenUtils.getTokenInfo(token);
        String username = tokenInfo.getClaim("username").asString();
        String id = tokenInfo.getClaim("id").asString();
        String newUsername = user.getUsername();
        userMapper.changeUsername(newUsername, username);
        userMapper.changeUsernameUpdateMessage(newUsername, username);
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("username", newUsername);
        httpServletResponse.addHeader("token", JwtTokenUtils.getToken(payload));
        return ResultData.success(true);
    }
}
