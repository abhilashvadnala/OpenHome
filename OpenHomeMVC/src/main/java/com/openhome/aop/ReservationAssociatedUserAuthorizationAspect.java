package com.openhome.aop;

import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.openhome.aop.helper.ArgsFinder;
import com.openhome.dao.ReservationDAO;
import com.openhome.data.Reservation;
import com.openhome.session.SessionManager;

@Aspect
@Component
@Order(0)
public class ReservationAssociatedUserAuthorizationAspect {

	@Autowired
	ReservationDAO reservationDao;
	
	@Autowired
	SessionManager sessionManager;

	@Around("@annotation(com.openhome.aop.helper.annotation.ReservationAssociatedUserLoginRequired)")
	public Object rightUserLoginRequired(ProceedingJoinPoint joinPoint) throws Throwable {
		
		try {
			HttpSession httpSession = ArgsFinder.getHttpSession(joinPoint.getArgs());
			if(sessionManager.hasUserLogin(httpSession) == false) {
				throw new IllegalAccessError("Login Required");
			}

			Long reservationId = ArgsFinder.findArg(joinPoint.getArgs(), Long.class);
			Reservation reservation = reservationDao.getOne(reservationId);
			Long guestId = sessionManager.getGuestId(httpSession);
			if(guestId != null) {
				System.out.println(reservation.getGuest().getId() +" | "+guestId);
				if(reservation.getGuest().getId().equals(guestId) == false) {
					throw new IllegalAccessError("Associated Login Required");
				}
			}else {
				if(reservation.getPlace().getHost().getId().equals(sessionManager.getHostId(httpSession)) == false) {
					throw new IllegalAccessError("Associated Login Required");
				}
			}
			return joinPoint.proceed();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "index";
		
	 }
	

	@Around("@annotation(com.openhome.aop.helper.annotation.ReservationAssociatedGuestLoginRequired)")
	public Object rightGuestLoginRequired(ProceedingJoinPoint joinPoint) throws Throwable {
		
		try {
			HttpSession httpSession = ArgsFinder.getHttpSession(joinPoint.getArgs());
			if(sessionManager.hasUserLogin(httpSession) == false) {
				throw new IllegalAccessError("Login Required");
			}

			Long reservationId = ArgsFinder.findArg(joinPoint.getArgs(), Long.class);
			Reservation reservation = reservationDao.getOne(reservationId);
			Long guestId = sessionManager.getGuestId(httpSession);
			if(guestId != null) {
				System.out.println(reservation.getGuest().getId() +" | "+guestId);
				if(reservation.getGuest().getId().equals(guestId) == false) {
					throw new IllegalAccessError("Associated Login Required");
				}
			}else {
				throw new IllegalAccessError("Associated Login Required");
			}
			return joinPoint.proceed();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "index";
		
	 }
	
	
}