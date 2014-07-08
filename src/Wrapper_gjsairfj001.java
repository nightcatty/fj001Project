
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;

import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

public class Wrapper_gjsairfj001 implements QunarCrawler {
	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("SUV");
		searchParam.setArr("SYD");
		searchParam.setDepDate("2014-08-01");
		searchParam.setRetDate("2014-08-05");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		Wrapper_gjsairfj001 wrapper = new Wrapper_gjsairfj001();
		String html = wrapper.getHtml(searchParam);
		ProcessResultInfo result = wrapper.process(html, searchParam);

	}

	@Override
	public BookingResult getBookingInfo(FlightSearchParam param) {
		BookingResult bookResult = new BookingResult();
		BookingInfo bookInfo = new BookingInfo();
		String[] depDataStrs = param.getDepDate().split("-");
		String depYear = depDataStrs[0];
		String depMonth = depDataStrs[1];
		String depDay = depDataStrs[2];

		String[] retDataStrs = param.getRetDate().split("-");
		String retYear = retDataStrs[0];
		String retMonth = retDataStrs[1];
		String retDay = retDataStrs[2];

		String getUrl = "http://booking.fijiairways.com/FJOnline/AirLowFareSearchExternal.do?validateAction=AirLowFareSearch&tripType=RT&searchType=FARE&cabinClass=Economy&pos=CONSUMER_FIJI&OSI=-INET+POS+CN&flexibleSearch=True&directFlightsOnly=False&redemption=False&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1&guestTypes%5B1%5D.type=CHD&guestTypes%5B1%5D.amount=0&guestTypes%5B2%5D.type=INF&guestTypes%5B2%5D.amount=0"
				+ String.format(
						"&outboundOption.originLocationCode=%s&outboundOption.destinationLocationCode=%s&outboundOption.departureDay=%s&outboundOption.departureMonth=%s&outboundOption.departureYear=%s&outboundOption.departureTime=NA"
								+ "&inboundOption.departureDay=%s&inboundOption.departureMonth=%s&inboundOption.departureYear=%s",
						param.getDep(), param.getArr(), depDay, depMonth,
						depYear, retDay, retMonth, retYear);

		bookInfo.setAction(getUrl);
		bookInfo.setMethod("get");
		bookResult.setData(bookInfo);
		return bookResult;
	}

	@Override
	public String getHtml(FlightSearchParam param) {
		QFGetMethod firstGet = null;
		QFGetMethod secondGet = null;
		QFPostMethod post = null;
		try {
			QFHttpClient httpClient = new QFHttpClient(param, false);
			String[] depDataStrs = param.getDepDate().split("-");
			String depYear = depDataStrs[0];
			String depMonth = depDataStrs[1];
			String depDay = depDataStrs[2];

			String[] retDataStrs = param.getRetDate().split("-");
			String retYear = retDataStrs[0];
			String retMonth = retDataStrs[1];
			String retDay = retDataStrs[2];

			String getUrl = "http://booking.fijiairways.com/FJOnline/AirLowFareSearchExternal.do?validateAction=AirLowFareSearch&tripType=RT&searchType=FARE&cabinClass=Economy&pos=CONSUMER_FIJI&OSI=-INET+POS+CN&flexibleSearch=True&directFlightsOnly=False&redemption=False&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1&guestTypes%5B1%5D.type=CHD&guestTypes%5B1%5D.amount=0&guestTypes%5B2%5D.type=INF&guestTypes%5B2%5D.amount=0"
					+ String.format(
							"&outboundOption.originLocationCode=%s&outboundOption.destinationLocationCode=%s&outboundOption.departureDay=%s&outboundOption.departureMonth=%s&outboundOption.departureYear=%s&outboundOption.departureTime=NA"
									+ "&inboundOption.departureDay=%s&inboundOption.departureMonth=%s&inboundOption.departureYear=%s",
							param.getDep(), param.getArr(), depDay, depMonth,
							depYear, retDay, retMonth, retYear);
			firstGet = new QFGetMethod(getUrl);
			int status = httpClient.executeMethod(firstGet);
			String firstGetHtml = firstGet.getResponseBodyAsString();

			String jsessionid = firstGetHtml.substring(
					firstGetHtml.indexOf("jsessionid=") + 11,
					firstGetHtml.indexOf("jsessionid=") + 43);
			String postUrl = "http://booking.fijiairways.com/FJOnline/AirLowFareSearchExt.do;jsessionid="
					+ jsessionid;
			post = new QFPostMethod(postUrl);
			NameValuePair[] names = { new NameValuePair("ajaxAction", "true") };
			post.setRequestBody(names);
			post.setRequestHeader("Referer", getUrl);
			post.getParams().setContentCharset("UTF-8");
			// String cookie =
			// StringUtils.join(httpClient.getState().getCookies(),";");
			// httpClient.getState().clearCookies();
			// firstGet.addRequestHeader("Cookie",cookie);
			httpClient.executeMethod(post);

			String postHtml = post.getResponseBodyAsString();
			if (postHtml.indexOf("success") > 0) {
				// String secondJsessionid =
				// postHtml.substring(postHtml.indexOf("jsessionid=")+11,
				// postHtml.indexOf("jsessionid=")+43);
				String secondGetUrl = "http://booking.fijiairways.com/FJOnline/AirFareFamiliesFlexibleForward.do";
				secondGet = new QFGetMethod(secondGetUrl);

				// cookie =
				// StringUtils.join(httpClient.getState().getCookies(),";");
				// // httpClient.getState().clearCookies();
				// secondGet.addRequestHeader("Cookie",cookie);
				httpClient.executeMethod(secondGet);
				String secondGetHtml = secondGet.getResponseBodyAsString();
				return secondGetHtml;
			}
			return "";

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != firstGet) {
				firstGet.releaseConnection();
			}
		}
		return "Exception";
	}

	@Override
	public ProcessResultInfo process(String html, FlightSearchParam param) {
		ProcessResultInfo result = new ProcessResultInfo();
		if ("Exception".equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		if (html.contains("'http://www.fijiairways.com/online-booking-error/?errorCode=INVALID_SEARCH_CRITERIA'")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
			return result;
		} else if (html.contains("No Flights Found")) {
			result.setRet(false);
			result.setStatus(Constants.NO_RESULT);
			return result;
		} else {
			try {
				List<RoundTripFlightInfo> roundTripFlightList = new ArrayList<RoundTripFlightInfo>();
				String goTableStr = StringUtils.substringBetween(html,
						"<div id=\"resultsFFBlock1\" class=\"resultsArea\">",
						"<div class=\"footnote\">");
				List<OneWayFlightInfo> goFlight = getFilghtList(goTableStr,
						param);
				String backTableStr = StringUtils.substringBetween(html,
						"<div id=\"resultsFFBlock2\" class=\"resultsArea\">",
						"<div class=\"footnote\">");
				List<OneWayFlightInfo> backFlight = getFilghtList(backTableStr,
						param);

				// 两层循环，对去程和返程list做笛卡尔积得到组合后的所有往返航程
				for (OneWayFlightInfo obfl : goFlight) {
					for (OneWayFlightInfo rtfl : backFlight) {
						RoundTripFlightInfo round = new RoundTripFlightInfo();
						round.setInfo(obfl.getInfo());// 去程航段信息
						round.setOutboundPrice(obfl.getDetail().getPrice());// 去程价格
						round.setReturnedPrice(rtfl.getDetail().getPrice());// 返程价格
						FlightDetail detail = new FlightDetail();
						detail = obfl.getDetail();
						detail.setPrice(sum(obfl.getDetail().getPrice(), rtfl
								.getDetail().getPrice()));// 往返总价格
						// detail.setPrice(obfl.getDetail().getPrice()+rtfl.getDetail().getPrice());//往返总价格
						// detail.setTax(obfl.getDetail().getTax()+rtfl.getDetail().getTax());//往返总税费
						detail.setTax(sum(obfl.getDetail().getTax(), rtfl
								.getDetail().getTax()));
						round.setDetail(detail); // 将设置后的去程信息装入往返中
						round.setRetdepdate(rtfl.getDetail().getDepdate());// 返程日期
						round.setRetflightno(rtfl.getDetail().getFlightno());// 返程航班号list
						round.setRetinfo(rtfl.getInfo());// 返程信息
						roundTripFlightList.add(round);// 添加到list
					}
				}

				result.setRet(true);
				result.setStatus(Constants.SUCCESS);
				result.setData(roundTripFlightList);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				result.setRet(false);
				result.setStatus(Constants.PARSING_FAIL);
				return result;
			}

		}
	}

	private double sum(double d1, double d2) {
		BigDecimal bd1 = new BigDecimal(Double.toString(d1));
		BigDecimal bd2 = new BigDecimal(Double.toString(d2));
		return bd1.add(bd2).doubleValue();
	}

	private List<OneWayFlightInfo> getFilghtList(String tableHtml,
			FlightSearchParam param) throws ParseException {
		String tbodyStr = StringUtils.substringBetween(tableHtml, "<tbody>",
				"</tbody>");
		List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
		String regtr = "\\s*<tr class=\"";
		String[] trs = tbodyStr.split(regtr);
		for (int i = 0; i < trs.length; ++i) {
			String flightInfo = trs[i].replaceAll("\\s", "");
			if (StringUtils.isBlank(flightInfo)) {
				continue;
			}
			// 转飞
			{
				OneWayFlightInfo flight = new OneWayFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();

				flightDetail.setMonetaryunit("FJD");
				double price = getPrice(flightInfo);
				List<String> flightNoList = new ArrayList<String>();
				flightDetail.setWrapperid("gjsairfj001");
				flightDetail.setTax(0);
				flightDetail.setArrcity(param.getArr());
				flightDetail.setDepcity(param.getDep());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;

				date = format.parse(param.getDepDate());
				flightDetail.setDepdate(date);
				flightDetail.setFlightno(flightNoList);
				flightDetail.setPrice(price);

				FlightSegement seg = new FlightSegement();
				String flightNo = StringUtils.substringBetween(flightInfo,
						"returnfalse;\">", "</a>");
				flightNoList.add(flightNo);

				String depart = StringUtils.substringBetween(flightInfo,
						"<tdclass=\"colDepart\"><div>", "</div></td>")
						.substring(0, 5);
				String arrive = StringUtils.substringBetween(flightInfo,
						"<tdclass=\"colArrive\"><div>", "</div></td>")
						.substring(0, 5);
				String airportsStr = StringUtils.substringBetween(flightInfo,
						"\"style=\"display:none\">", "</span>");
				String[] airPorts = airportsStr.split("-");

				seg.setFlightno(flightNo);
				seg.setDeptime(depart);
				seg.setDepairport(airPorts[0]);
				seg.setArrtime(arrive);
				seg.setArrairport(airPorts[1]);
				seg.setDepDate(param.getDepDate());
				seg.setArrDate(param.getDepDate());
				seg.setCompany("斐济航空");
				segs.add(seg);

				String duration = StringUtils.substringBetween(flightInfo,
						"<tdclass=\"colDuration\"><div>", "</div></td>");

				if (flightInfo.indexOf("combineRows") != -1) {
					flightInfo = trs[++i].replaceAll("\\s", "");

					FlightSegement nextSeg = new FlightSegement();
					flightNo = StringUtils.substringBetween(flightInfo,
							"returnfalse;\">", "</a>");
					flightNoList.add(flightNo);
					String connectDepart = StringUtils.substringBetween(
							flightInfo, "<tdclass=\"colDepart\"><div>",
							"</div></td>").substring(0, 5);
					String connectArrive = StringUtils.substringBetween(
							flightInfo, "<tdclass=\"colArrive\"><div>",
							"</div></td>").substring(0, 5);
					String connectAirports = StringUtils.substringBetween(
							flightInfo, "\"style=\"display:none\">", "</span>");
					airPorts = connectAirports.split("-");
					String connectDuration = StringUtils.substringBetween(
							flightInfo, "<tdclass=\"colDuration\"><div>",
							"<br/");
					String totalTime = StringUtils.substringBetween(flightInfo,
							"<spanclass=\"totalTime\">", "</span>");

					nextSeg.setFlightno(flightNo);
					nextSeg.setDeptime(connectDepart);
					nextSeg.setDepairport(airPorts[0]);
					nextSeg.setArrtime(connectArrive);
					nextSeg.setArrairport(airPorts[1]);
					nextSeg.setDepDate(param.getDepDate());
					nextSeg.setArrDate(param.getDepDate());
					nextSeg.setCompany("斐济航空");
					segs.add(nextSeg);
				}
				flight.setDetail(flightDetail);
				flight.setInfo(segs);
				flightList.add(flight);
			}

		}
		return flightList;

	}

	private double getPrice(String flightInfo) {
		double price = Double.MAX_VALUE;
		String priceStr = flightInfo
				.replaceAll(
						"<td><divclass=\"colPrice\"><labelfor=\"flightSelectGr[^\"]*\">",
						"_price_");
		while (priceStr.contains("_price_")) {
			double newPrice = Double.parseDouble(StringUtils.substringBetween(
					priceStr, "_price_", "</label></div></td>").replaceAll(",",
					""));
			if (newPrice < price) {
				price = newPrice;
			}
			priceStr = StringUtils.substringAfter(priceStr, "_price_");
		}
		return price;
	}

	private String getArriveDate(String depTime, String duration) {
		String[] depTimes = depTime.split(":");
		String durationH = duration.substring(0, duration.indexOf('h'));
		String durationM = duration.substring(duration.indexOf('h') + 1,
				duration.indexOf('m'));
		int depH = Integer.parseInt(depTimes[0]);
		int depM = Integer.parseInt(depTimes[1]);
		int durH = Integer.parseInt(durationH);
		int durM = Integer.parseInt(durationM);

		int resultH = depH + durH;
		int resultM = depM + durM;
		if (resultM >= 60) {
			resultH++;
			resultM -= 60;
		}
		return resultH + ":" + resultM;
	}

}