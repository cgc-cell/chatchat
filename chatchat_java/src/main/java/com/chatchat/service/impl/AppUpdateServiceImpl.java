package com.chatchat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.enums.AppUpdateFileTypeEnum;
import com.chatchat.entity.enums.AppUpdateStatusEnum;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.exception.BusinessException;
import org.springframework.stereotype.Service;

import com.chatchat.entity.enums.PageSize;
import com.chatchat.entity.query.AppUpdateQuery;
import com.chatchat.entity.po.AppUpdate;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.query.SimplePage;
import com.chatchat.mappers.AppUpdateMapper;
import com.chatchat.service.AppUpdateService;
import com.chatchat.utils.StringTools;
import org.springframework.web.multipart.MultipartFile;


/**
 * app发布更新 业务接口实现
 */
@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

	@Resource
	private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;
	@Resource
	AppConfig appConfig;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<AppUpdate> findListByParam(AppUpdateQuery param) {
		return this.appUpdateMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AppUpdateQuery param) {
		return this.appUpdateMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<AppUpdate> list = this.findListByParam(param);
		PaginationResultVO<AppUpdate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(AppUpdate bean) {
		return this.appUpdateMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<AppUpdate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.appUpdateMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<AppUpdate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.appUpdateMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(AppUpdate bean, AppUpdateQuery param) {
		StringTools.checkParam(param);
		return this.appUpdateMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AppUpdateQuery param) {
		StringTools.checkParam(param);
		return this.appUpdateMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public AppUpdate getAppUpdateById(Integer id) {
		return this.appUpdateMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
		return this.appUpdateMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteAppUpdateById(Integer id) {
		return this.appUpdateMapper.deleteById(id);
	}

	/**
	 * 根据Version获取对象
	 */
	@Override
	public AppUpdate getAppUpdateByVersion(String version) {
		return this.appUpdateMapper.selectByVersion(version);
	}

	/**
	 * 根据Version修改
	 */
	@Override
	public Integer updateAppUpdateByVersion(AppUpdate bean, String version) {
		return this.appUpdateMapper.updateByVersion(bean, version);
	}

	/**
	 * 根据Version删除
	 */
	@Override
	public Integer deleteAppUpdateByVersion(String version) {
		return this.appUpdateMapper.deleteByVersion(version);
	}


	@Override
	public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
		AppUpdateFileTypeEnum fileTypeEnum=AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
		if(fileTypeEnum==null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		AppUpdateQuery updateQuery=new AppUpdateQuery();
		updateQuery.setOrderBy("version desc");
		updateQuery.setSimplePage(new SimplePage(0,1));
		List<AppUpdate> list=this.appUpdateMapper.selectList(updateQuery);
		if(list!=null &&!list.isEmpty()){
			AppUpdate lastUpdate=list.get(0);
			long dbVersion=Long.parseLong(lastUpdate.getVersion().replace(".",""));
			Long newVersion = Long.parseLong(appUpdate.getVersion().replace(".",""));
			if(appUpdate.getId()==null&&dbVersion>=newVersion){
				throw new BusinessException("当前版本必须大于历史版本");
			}
			if(appUpdate.getId()!=null&&dbVersion>=newVersion&&!appUpdate.getId().equals(lastUpdate.getId())){
				throw new BusinessException("当前版本必须大于历史版本");
			}
			AppUpdate versionDb=appUpdateMapper.selectByVersion(appUpdate.getVersion());
			if (versionDb!=null&&!versionDb.getId().equals(appUpdate.getId())) {
				throw new BusinessException("版本号已存在");
			}
		}
		if(appUpdate.getId()==null){
			appUpdate.setCreateTime(new Date());
			appUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
			appUpdateMapper.insert(appUpdate);
		}else {
			appUpdate.setStatus(null);
			appUpdate.setGrayscaleUid(null);
			appUpdateMapper.updateById(appUpdate,appUpdate.getId());
		}
		if (file != null) {
			String basePath = appConfig.getProjectFolder()+ Constants.APP_UPDATE_FOLDER;
			File targetFolder = new File(basePath);
			if(!targetFolder.exists()){
				targetFolder.mkdirs();
			}

			String fileName = targetFolder.getAbsolutePath()+"/"+appUpdate.getId()+Constants.APP_EXE_SUFFIX;
			file.transferTo(new File(fileName));
		}
	}

	@Override
	public void postUpdate(Integer id, Integer status, String grayScaleUid) {
		AppUpdateStatusEnum statusEnum=AppUpdateStatusEnum.getByStatus(status);
		if(statusEnum==null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (AppUpdateStatusEnum.GRAYSCALE.equals(statusEnum)&&StringTools.isEmpty(grayScaleUid)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (!AppUpdateStatusEnum.GRAYSCALE.equals(statusEnum)) {
			grayScaleUid="";
		}
		AppUpdate appUpdate=new AppUpdate();
		appUpdate.setGrayscaleUid(grayScaleUid);
		appUpdate.setStatus(status);
		this.appUpdateMapper.updateById(appUpdate,id);
	}

	@Override
	public AppUpdate getLastUpdate(String appVersion, String id) {
		return appUpdateMapper.selectTheLastUpdate(appVersion,id);
	}
}