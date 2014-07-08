

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
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

public class Wrapper_gjdairfj001 implements QunarCrawler {
	public static void main(String[] args) {
		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("SUV");
		searchParam.setArr("SYD");
		searchParam.setDepDate("2014-07-21");
		searchParam.setTimeOut("60000");
		searchParam.setToken("");
		Wrapper_gjdairfj001 wrapper = new Wrapper_gjdairfj001();
		String html = wrapper.getHtml(searchParam);
		ProcessResultInfo result = wrapper.process(html, searchParam);

		if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
			List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result
					.getData();
			for (OneWayFlightInfo in : flightList) {
				System.out.println("************" + in.getInfo().toString());
				System.out.println("++++++++++++" + in.getDetail().toString());
			}
		} else {
			System.out.println(result.getStatus());
		}

	}

	@Override
	public BookingResult getBookingInfo(FlightSearchParam param) {
		BookingResult bookResult = new BookingResult();
		BookingInfo bookInfo = new BookingInfo();
		String[] depDataStrs = param.getDepDate().split("-");
		String depYear = depDataStrs[0];
		String depMonth = depDataStrs[1];
		String depDay = depDataStrs[2];
		String getUrl = "http://booking.fijiairways.com/FJOnline/AirLowFareSearchExternal.do?validateAction=AirLowFareSearch&tripType=OW&searchType=FARE&cabinClass=Economy&pos=CONSUMER_FIJI&OSI=-INET+POS+CN&flexibleSearch=True&directFlightsOnly=False&redemption=False&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1&guestTypes%5B1%5D.type=CHD&guestTypes%5B1%5D.amount=0&guestTypes%5B2%5D.type=INF&guestTypes%5B2%5D.amount=0&"
				+ String.format(
						"outboundOption.originLocationCode=%s&outboundOption.destinationLocationCode=%s&outboundOption.departureDay=%s&outboundOption.departureMonth=%s&outboundOption.departureYear=%s&outboundOption.departureTime=NA",
						param.getDep(), param.getArr(), depDay, depMonth,
						depYear);
		
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
			String getUrl = "http://booking.fijiairways.com/FJOnline/AirLowFareSearchExternal.do?validateAction=AirLowFareSearch&tripType=OW&searchType=FARE&cabinClass=Economy&pos=CONSUMER_FIJI&OSI=-INET+POS+CN&flexibleSearch=True&directFlightsOnly=False&redemption=False&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1&guestTypes%5B1%5D.type=CHD&guestTypes%5B1%5D.amount=0&guestTypes%5B2%5D.type=INF&guestTypes%5B2%5D.amount=0&"
					+ String.format(
							"outboundOption.originLocationCode=%s&outboundOption.destinationLocationCode=%s&outboundOption.departureDay=%s&outboundOption.departureMonth=%s&outboundOption.departureYear=%s&outboundOption.departureTime=NA",
							param.getDep(), param.getArr(), depDay, depMonth,
							depYear);
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
			httpClient.executeMethod(post);

			String postHtml = post.getResponseBodyAsString();
			if (postHtml.indexOf("success") > 0) {
				String secondGetUrl = "http://booking.fijiairways.com/FJOnline/AirFareFamiliesFlexibleForward.do";
				secondGet = new QFGetMethod(secondGetUrl);

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
		}

		else if (html.contains("No Flights Found")) {
			result.setRet(false);
			result.setStatus(Constants.NO_RESULT);
			return result;
		}
		else  {
			try {
				String tableStr = StringUtils.substringBetween(html,
						"<div id=\"resultsFFBlock1\" class=\"resultsArea\">",
						"<div class=\"footnote\">");
				String tbodyStr = StringUtils.substringBetween(tableStr,
						"<tbody>", "</tbody>");
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
						flightDetail.setWrapperid("gjdairfj001");
						flightDetail.setTax(0);
						flightDetail.setArrcity(param.getArr());
						flightDetail.setDepcity(param.getDep());
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd");
						Date date = null;

						date = format.parse(param.getDepDate());
						flightDetail.setDepdate(date);
						flightDetail.setFlightno(flightNoList);
						flightDetail.setPrice(price);

						FlightSegement seg = new FlightSegement();
						String flightNo = StringUtils.substringBetween(
								flightInfo, "returnfalse;\">", "</a>");
						flightNoList.add(flightNo);

						String depart = StringUtils.substringBetween(
								flightInfo, "<tdclass=\"colDepart\"><div>",
								"</div></td>");
						String arrive = StringUtils.substringBetween(
								flightInfo, "<tdclass=\"colArrive\"><div>",
								"</div></td>");
						String airportsStr = StringUtils.substringBetween(
								flightInfo, "\"style=\"display:none\">",
								"</span>");
						String[] airPorts = airportsStr.split("-");
						// String duration =
						// StringUtils.substringBetween(flightInfo,"<tdclass=\"colDuration\"><div>","</div>");

						seg.setFlightno(flightNo);
						seg.setDeptime(depart.substring(0,5));
						seg.setDepairport(airPorts[0]);
						seg.setArrtime(arrive.substring(0,5));
						seg.setArrairport(airPorts[1]);
						seg.setDepDate(param.getDepDate());
						seg.setArrDate(param.getDepDate());
						seg.setCompany("斐济航空");
						segs.add(seg);

						if (flightInfo.indexOf("combineRows") != -1){
							flightInfo = trs[++i].replaceAll("\\s", "");
	
							FlightSegement nextSeg = new FlightSegement();
							flightNo = StringUtils.substringBetween(flightInfo,
									"returnfalse;\">", "</a>");
							flightNoList.add(flightNo);
							String connectDepart = StringUtils.substringBetween(
									flightInfo, "<tdclass=\"colDepart\"><div>",
									"</div></td>");
							String connectArrive = StringUtils.substringBetween(
									flightInfo, "<tdclass=\"colArrive\"><div>",
									"</div></td>");
							String connectAirports = StringUtils.substringBetween(
									flightInfo, "\"style=\"display:none\">",
									"</span>");
							airPorts = connectAirports.split("-");
							String connectDuration = StringUtils.substringBetween(
									flightInfo, "<tdclass=\"colDuration\"><div>",
									"<br/");
							String totalTime = StringUtils.substringBetween(
									flightInfo, "<spanclass=\"totalTime\">",
									"</span>");
	
							nextSeg.setFlightno(flightNo);
							nextSeg.setDeptime(connectDepart.substring(0,5));
							nextSeg.setDepairport(airPorts[0]);
							nextSeg.setArrtime(connectArrive.substring(0,5));
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
				result.setData(flightList);
				result.setRet(true);
				result.setStatus(Constants.SUCCESS);
				return result;
			} catch (ParseException e) {
				e.printStackTrace();
				result.setRet(false);
				result.setStatus(Constants.PARSING_FAIL);
				return result;
			}

		}
	}
	private double getPrice(String flightInfo) {
		double price = Double.MAX_VALUE;
		String priceStr = flightInfo.replaceAll("<td><divclass=\"colPrice\"><labelfor=\"flightSelectGr[^\"]*\">", "_price_");
		while(priceStr.contains("_price_")){
			double newPrice = Double.parseDouble(StringUtils.substringBetween(priceStr, "_price_","</label></div></td>").replaceAll(",", ""));
			if(newPrice < price){
				price = newPrice;
			}
			priceStr = StringUtils.substringAfter(priceStr, "_price_");
		}
		return price;
	}

}
