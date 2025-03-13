package com.chatchat.entity.vo;

import com.chatchat.entity.enums.DateTimePatternEnum;
import com.chatchat.utils.DateUtil;
import com.chatchat.utils.StringTools;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * app发布更新
 */
public class AppUpdateVO implements Serializable {


	/**
	 * 自增id
	 */
	private Integer id;

	/**
	 * 版本号
	 */
	private String version;

	/**
	 * 更新描述
	 */
	private String updateDesc;

	/**
	 * 文件类型，0：本地文件，1外链
	 */
	private Integer fileType;

	/**
	 * 外链地址
	 */
	private String outerLink;

	private String[] updateDescArray;

	private Long size;

	private String fileName;

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String[] getUpdateDescArray() {
		if (!StringTools.isEmpty(updateDesc)) {
			updateDescArray = updateDesc.split("\\|");
		}
		return updateDescArray;
	}

	public void setUpdateDescArray(String[] updateDescArray) {
		this.updateDescArray = updateDescArray;
	}



	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setVersion(String version){
		this.version = version;
	}

	public String getVersion(){
		return this.version;
	}

	public void setUpdateDesc(String updateDesc){
		this.updateDesc = updateDesc;
	}

	public String getUpdateDesc(){
		return this.updateDesc;
	}


	public void setFileType(Integer fileType){
		this.fileType = fileType;
	}

	public Integer getFileType(){
		return this.fileType;
	}

	public void setOuterLink(String outerLink){
		this.outerLink = outerLink;
	}

	public String getOuterLink(){
		return this.outerLink;
	}

	@Override
	public String toString (){
		return "自增id:"+(id == null ? "空" : id)+"，版本号:"+(version == null ? "空" : version)+"，更新描述:"+(updateDesc == null ? "空" : updateDesc)+"，文件类型，0：本地文件，1外链:"+(fileType == null ? "空" : fileType)+"，外链地址:"+(outerLink == null ? "空" : outerLink);
	}
}
