package com.chatchat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.chatchat.entity.enums.BeautyAccountStatusEnum;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.query.UserInfoQuery;
import com.chatchat.exception.BusinessException;
import com.chatchat.mappers.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatchat.entity.enums.PageSize;
import com.chatchat.entity.query.UserInfoBeautyQuery;
import com.chatchat.entity.po.UserInfoBeauty;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.query.SimplePage;
import com.chatchat.mappers.UserInfoBeautyMapper;
import com.chatchat.service.UserInfoBeautyService;
import com.chatchat.utils.StringTools;


/**
 * 靓号表 业务接口实现
 */
@Service("userInfoBeautyService")
public class UserInfoBeautyServiceImpl implements UserInfoBeautyService {

	@Resource
	private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;
    @Autowired
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfoBeauty> findListByParam(UserInfoBeautyQuery param) {
		return this.userInfoBeautyMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoBeautyQuery param) {
		return this.userInfoBeautyMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfoBeauty> findListByPage(UserInfoBeautyQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfoBeauty> list = this.findListByParam(param);
		PaginationResultVO<UserInfoBeauty> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfoBeauty bean) {
		return this.userInfoBeautyMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfoBeauty> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoBeautyMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfoBeauty> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoBeautyMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfoBeauty bean, UserInfoBeautyQuery param) {
		StringTools.checkParam(param);
		return this.userInfoBeautyMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoBeautyQuery param) {
		StringTools.checkParam(param);
		return this.userInfoBeautyMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public UserInfoBeauty getUserInfoBeautyById(Integer id) {
		return this.userInfoBeautyMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateUserInfoBeautyById(UserInfoBeauty bean, Integer id) {
		return this.userInfoBeautyMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteUserInfoBeautyById(Integer id) {
		return this.userInfoBeautyMapper.deleteById(id);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfoBeauty getUserInfoBeautyByUserId(String userId) {
		return this.userInfoBeautyMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId) {
		return this.userInfoBeautyMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoBeautyByUserId(String userId) {
		return this.userInfoBeautyMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfoBeauty getUserInfoBeautyByEmail(String email) {
		return this.userInfoBeautyMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email) {
		return this.userInfoBeautyMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoBeautyByEmail(String email) {
		return this.userInfoBeautyMapper.deleteByEmail(email);
	}

	@Override
	public void saveAccount(UserInfoBeauty userInfoBeauty) {
		if (userInfoBeauty.getId()!=null) {
			UserInfoBeauty dbuserInfoBeauty = userInfoBeautyMapper.selectById(userInfoBeauty.getId());
			if (dbuserInfoBeauty==null||BeautyAccountStatusEnum.USED.getStatus().equals(dbuserInfoBeauty.getStatus())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}

		}

		UserInfoBeauty dbuserInfoBeauty = userInfoBeautyMapper.selectByEmail(userInfoBeauty.getEmail());
		if(dbuserInfoBeauty!=null&&userInfoBeauty.getId()==null) {
			throw new BusinessException("靓号邮箱已经存在");
		}

        if (userInfoBeauty.getId() != null && dbuserInfoBeauty != null && dbuserInfoBeauty.getId() != null&&!userInfoBeauty.getId().equals(dbuserInfoBeauty.getId())) {
			throw new BusinessException("靓号邮箱已经存在");
        }

		dbuserInfoBeauty = userInfoBeautyMapper.selectByUserId(userInfoBeauty.getUserId());
		if(dbuserInfoBeauty!=null&&userInfoBeauty.getId()==null) {
			throw new BusinessException("靓号已经存在");
		}

		if (userInfoBeauty.getId() != null && dbuserInfoBeauty != null && dbuserInfoBeauty.getId() != null&&!userInfoBeauty.getId().equals(dbuserInfoBeauty.getId())) {
			throw new BusinessException("靓号已经存在");
		}

		UserInfo userInfo = userInfoMapper.selectByEmail(userInfoBeauty.getEmail());
		if(userInfo!=null) {
			throw new BusinessException("邮箱已经被使用");
		}
		userInfo = userInfoMapper.selectByUserId(userInfoBeauty.getUserId());
		if(userInfo!=null) {
			throw new BusinessException("靓号已经被使用");
		}
		if(userInfoBeauty.getId()!=null) {
			this.userInfoBeautyMapper.updateById(userInfoBeauty,userInfoBeauty.getId());
		}else {
			userInfoBeauty.setStatus(BeautyAccountStatusEnum.NOT_USED.getStatus());
			this.userInfoBeautyMapper.insert(userInfoBeauty);
		}

    }
}