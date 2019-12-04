package com.openhome.controllers.space;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.openhome.FileSystem;
import com.openhome.aop.helper.annotation.ValidSpaceId;
import com.openhome.controllers.helper.ControllerHelper;
import com.openhome.aop.helper.annotation.SpaceHostLoginRequired;
import com.openhome.dao.ImageDAO;
import com.openhome.dao.SpaceDAO;
import com.openhome.dao.SpaceDetailsDAO;
import com.openhome.data.Host;
import com.openhome.data.Image;
import com.openhome.data.Space;
import com.openhome.data.SpaceDetails;
import com.openhome.session.SessionManager;

@Controller
public class SpaceImageUpdateController {

	@Autowired
	SpaceDAO spaceDao;
	
	@Autowired
	SpaceDetailsDAO spaceDetailsDao;
	
	@Autowired
	SessionManager sessionManager;
	
	@Autowired
	public ImageDAO imageDao;
	
	@Autowired
	FileSystem fileSystem;
	
	@RequestMapping(value="/space/updateSpaceImages",method=RequestMethod.GET)
	@ValidSpaceId
	@SpaceHostLoginRequired
	public String updateForm(@RequestParam(value="spaceId",required=false) Long spaceId, @RequestParam(value="op") String op, Model model , HttpSession httpSession ) {
		System.out.println("SpaceImageUpdateController");
		Space s = null;
		
		if(op.equals("add")) {
			
			return "space/imagesUpdateAdd";
			
		} else if(op.equals("delete")) {

			return "space/imagesUpdateDelete";
			
		} else if(op.equals("rearrange")) {

			return "space/imagesUpdateRearrange";
			
		}
		
		return "space/imagesUpdate";
		
	}
	
	@RequestMapping(value="/space/updateSpaceImagesAdd",method=RequestMethod.POST)
	@ValidSpaceId
	@SpaceHostLoginRequired
	public String updateFormAdd(@RequestParam(value="spaceId",required=false) Long spaceId, Model model , HttpSession httpSession ,@RequestParam(value="image",required=false) MultipartFile image, @RequestParam(value="imageUrl",required=false) String imageUrl) {
		System.out.println("SpaceImageUpdateController");
		Space s = null;
		
		try {
			s = spaceDao.getOne(spaceId);
			Image imageObj;
			if(image == null) {
				if(imageUrl == null) {
					throw new IllegalArgumentException("No Image Provided");
				}
				imageObj = fileSystem.saveImage(imageUrl);
			}else {
				if(image.getSize()<1000) {
					throw new IllegalArgumentException("No Image Provided");
				}
				imageObj = fileSystem.saveImage(image);
			}
			
			SpaceDetails sd = s.getSpaceDetails();
			
			sd.addImage(imageObj);
			
			spaceDetailsDao.save(sd);
			
			model.addAttribute("space", s);
			model.addAttribute("close", "yes");
			return "space/imagesUpdateAdd";
		} catch (Exception e ) {
			return ControllerHelper.popupMessageAndRedirect(e.getMessage(), "");
		}
	}
	
	@RequestMapping(value="/space/updateSpaceImagesDelete",method=RequestMethod.POST)
	@ValidSpaceId
	@SpaceHostLoginRequired
	public String updateFormAdd(@RequestParam(value="spaceId",required=false) Long spaceId, Model model , HttpSession httpSession ,@RequestParam(value="deleteImageName",required=false) Long deleteImageId) {
		System.out.println("SpaceImageUpdateController");
		Space s = null;
		
		try {
			s = spaceDao.getOne(spaceId);
			
			if(deleteImageId == null) {
				throw new IllegalArgumentException("No Image Provided");
			}
			
			Image image = imageDao.getOne(deleteImageId);
			
			SpaceDetails sd = s.getSpaceDetails();
			
			if(sd.deleteImage(image)) {
				fileSystem.deleteImage(image);
				spaceDetailsDao.save(sd);
			}

			model.addAttribute("space", s);
			model.addAttribute("close", "yes");
			return "space/imagesUpdateDelete";
		} catch (Exception e ) {
			return ControllerHelper.popupMessageAndRedirect(e.getMessage(), "");
		}
	}
	
	@RequestMapping(value="/space/updateSpaceImagesRearrange",method=RequestMethod.POST)
	@ValidSpaceId
	@SpaceHostLoginRequired
	public String updateFormAdd(@RequestParam(value="spaceId",required=false) Long spaceId, Model model , HttpSession httpSession ,@RequestParam(value="images[]",required=false) List<Long> images) {
		System.out.println("SpaceImageUpdateController");
		Space s = null;
		
		try {
			s = spaceDao.getOne(spaceId);
			
			SpaceDetails sd = s.getSpaceDetails();
			
			System.out.println(images);
			
			if(images != null) {
				List<Image> prevImgList = sd.getImages();
				List<Image> newImgList = new ArrayList<Image>();
				for (Long imageId : images) {
					System.out.println("Looking for "+imageId);
					for (Image image : prevImgList) {
						System.out.println("Checking with "+image.getId());
						if(image.getId().equals(imageId)) {
							System.out.println("Image Found");
							newImgList.add(image);
							break;
						}
					}
				}
				
				for (int i = 0; i < prevImgList.size(); i++) {
					imageDao.updateImage(prevImgList.get(i).getId(),newImgList.get(i).getPublicId(),newImgList.get(i).getUrl());
				}
			}
			
			model.addAttribute("space", s);
			model.addAttribute("close", "yes");
			return "space/imagesUpdateRearrange";
		} catch (Exception e ) {
			return ControllerHelper.popupMessageAndRedirect(e.getMessage(), "");
		}
	}

	
}
