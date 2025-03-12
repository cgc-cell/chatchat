package com.chatchat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.enums.*;
import com.chatchat.entity.po.UserInfoBeauty;
import com.chatchat.entity.query.UserInfoBeautyQuery;
import com.chatchat.entity.vo.UserInfoVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.mappers.UserInfoBeautyMapper;
import com.chatchat.redis.RedisComponent;
import com.chatchat.redis.RedisUtils;
import com.chatchat.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatchat.entity.query.UserInfoQuery;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.query.SimplePage;
import com.chatchat.mappers.UserInfoMapper;
import com.chatchat.service.UserInfoService;
import com.chatchat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户信息表 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

	@Resource
	private AppConfig appConfig;
    @Autowired
    private RedisUtils redisUtils;
	@Resource
	private RedisComponent redisComponent;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickname, String password) {
		UserInfo userInfo=this.getUserInfoByEmail(email);
		if(userInfo!=null) {
			throw new BusinessException("邮箱账户已存在！");
		}
		String userID=StringTools.getUserId();
		UserInfoBeauty beautyAccount=  userInfoBeautyMapper.selectByEmail(email);
		boolean useBeautyAccount=null!=beautyAccount&& BeautyAccountStatusEnum.NOT_USED.getStatus().equals(beautyAccount.getStatus());
		if(useBeautyAccount) {
			userID=beautyAccount.getUserId();
		}
		Date curDate=new Date();
		userInfo=new UserInfo();
		userInfo.setUserId(userID);
		userInfo.setEmail(email);
		userInfo.setNickName(nickname);
		userInfo.setPassword(StringTools.encodeMD5(password));
		userInfo.setCreateTime(curDate);
		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		userInfo.setLastOffTime(curDate.getTime());
		userInfo.setJoinType(JoinTypeEnum.APPLY.getType());
		this.userInfoMapper.insert(userInfo);

		if(useBeautyAccount) {
			beautyAccount.setStatus(BeautyAccountStatusEnum.USED.getStatus());
			this.userInfoBeautyMapper.updateById(beautyAccount,beautyAccount.getId());
		}

		//TODO 创建机器人好友
	}

	@Override
	public UserInfoVO login(String email, String password) {
		UserInfo userInfo=this.getUserInfoByEmail(email);
		if(userInfo==null||!userInfo.getPassword().equals(password)) {
			throw new BusinessException("邮箱账户不存在或者账号密码不匹配！");
		}
		if(UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
			throw new BusinessException("该账户已被禁用！");
		}

		//TODO 查询我的群组
		//TODO 查询我的联系人

		TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(userInfo);

		Long lastHeartBeat =redisComponent.getUserHeartBeat(userInfo.getUserId());
		if(lastHeartBeat!=null) {
			throw new BusinessException("此账户已经在别处登录，请退出后再登录！");
		}
		String token = StringTools.encodeMD5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(Constants.LENGTH_22));

		tokenUserInfoDto.setToken(token);
		redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);


		UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
		userInfoVO.setToken(tokenUserInfoDto.getToken());
		userInfoVO.setAdmin(tokenUserInfoDto.isAdmin());

		return userInfoVO;


	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
		if (avatarFile != null) {
			String baseFolder = appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
			File targetFolder=new File(baseFolder+Constants.FILE_FOLDER_AVATAR);
			if(!targetFolder.exists()) {
				targetFolder.mkdirs();
			}
			String filePath = targetFolder.getPath()+"/"+userInfo.getUserId()+Constants.IMAGE_SUFFIX;
			avatarFile.transferTo(new File(filePath));
			avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
		}
		UserInfo dbUserInfo=this.userInfoMapper.selectByUserId(userInfo.getUserId());
		this.userInfoMapper.updateByUserId(userInfo,dbUserInfo.getUserId());
		String contactNameUpdate=null;
		if(!dbUserInfo.getNickName().equals(userInfo.getNickName())) {
			contactNameUpdate=userInfo.getNickName();
		}
		//TODO 更新会话中的昵称信息
	}

	@Override
	public void updateUserStatus(Integer status, String userId) {
		UserStatusEnum userStatus=UserStatusEnum.getByStatus(status);
		if(userStatus==null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setStatus(status);
		this.userInfoMapper.updateByUserId(userInfo,userId);

	}

	@Override
	public void forceOffLine(String userId) {
		// TODO 强制下线
	}

	private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
		TokenUserInfoDto tokenUserInfoDto=new TokenUserInfoDto();
		tokenUserInfoDto.setUserId(userInfo.getUserId());
		tokenUserInfoDto.setNickName(userInfo.getNickName());

		String adminEmail=appConfig.getAdminEmails();
		if (!StringTools.isEmpty(adminEmail)&& ArraysUtil.contains(adminEmail.split(","), userInfo.getEmail())) {
			tokenUserInfoDto.setAdmin(true);
		}
		else {
			tokenUserInfoDto.setAdmin(false);
		}
		return tokenUserInfoDto;
	}

}