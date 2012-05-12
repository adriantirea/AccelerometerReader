package com.syncserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.model.database.ActivityCompressed;
import com.model.database.ActivityReportEntry;
import com.model.database.DataSourceActivityCompressed;
import com.model.database.DataSourceReport;

public class SyncServer {

	public static final String importURL = "http://activitiesmonitor.appspot.com/allreports";
	public static final String exportURL = "http://activitiesmonitor.appspot.com/activitiesmonitor";
	public static final String createAccountURL = "http://activitiesmonitor.appspot.com/registeraccount";

	public static String createAccount(String username, String password, String email) {

		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(createAccountURL);

			JSONObject account = new JSONObject();
			account.put("username", username);
			account.put("password", password);
			account.put("email", email);

			StringEntity se = new StringEntity(account.toString());
			httpost.setEntity(se);
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");

			HttpResponse response = httpclient.execute(httpost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				StringBuilder builder = new StringBuilder();
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				JSONObject jsonObject = new JSONObject(builder.toString());
				Log.v("JSON Response", jsonObject.getString("message"));

				return jsonObject.getString("message");

			} else {
				Log.e("JSON Response", "Failed to register command!");
				return "Failed to create account";
			}

		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Failed";
	}

	public static String exportReports(String username, String password, Context context) {

		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(exportURL);

			JSONObject sendData = new JSONObject();
			JSONObject account = new JSONObject();
			account.put("username", username);
			account.put("password", password);
			sendData.put("user", account);

			DataSourceReport datasourceReport = new DataSourceReport(context);
			datasourceReport.open();
			DataSourceActivityCompressed datasourceACompressed = new DataSourceActivityCompressed(context);
			datasourceACompressed.open();

			List<ActivityReportEntry> localReports = datasourceReport.getAllActivityReports();
			JSONArray reportsSend = new JSONArray();
			for (ActivityReportEntry reportLocal : localReports) {
				JSONObject reportSend = new JSONObject();
				reportSend.put("startDate", reportLocal.getStartDate());
				reportSend.put("endDate", reportLocal.getEndDate());

				List<ActivityCompressed> activitiesLocal = datasourceACompressed.getAllActivitiesForReportID(reportLocal.getId());
				JSONArray activitiesSend = new JSONArray();
				for (ActivityCompressed aLocal : activitiesLocal) {
					JSONObject aSend = new JSONObject();
					aSend.put("name", aLocal.getName());
					aSend.put("value", aLocal.getValue());
					aSend.put("steps", aLocal.getSteps());
					activitiesSend.put(aSend);
				}
				reportSend.put("activities", activitiesSend);
				reportsSend.put(reportSend);
			}

			datasourceACompressed.close();
			datasourceReport.close();

			sendData.put("reports", reportsSend);
			StringEntity se = new StringEntity(sendData.toString());
			httpost.setEntity(se);
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");

			HttpResponse response = httpclient.execute(httpost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				StringBuilder builder = new StringBuilder();
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				JSONObject jsonObject = new JSONObject(builder.toString());
				Log.v("JSON Response", jsonObject.getString("message"));

				return jsonObject.getString("message");

			} else {
				Log.e("JSON Response", "Failed to register command!");
				return "Connection error "+ statusCode;
			}

		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Failed";
	}

	public static String importReports(String username, String password, Context context) {
		try {
			Log.v("startImport","");
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(importURL);

			JSONObject sendData = new JSONObject();
			JSONObject account = new JSONObject();
			account.put("username", username);
			account.put("password", password);
			sendData.put("user", account);

			StringEntity se = new StringEntity(sendData.toString());
			httpost.setEntity(se);
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");

			HttpResponse response = httpclient.execute(httpost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				StringBuilder builder = new StringBuilder();
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				JSONObject jsonData = new JSONObject(builder.toString());
				Log.v("JSON Response", builder.toString());

				if (jsonData.has("reports")){
					JSONArray reportsInfo = jsonData.getJSONArray("reports");

					DataSourceReport datasourceReport = new DataSourceReport(context);
					datasourceReport.open();
					DataSourceActivityCompressed datasourceACompressed = new DataSourceActivityCompressed(context);
					datasourceACompressed.open();

					List<ActivityReportEntry> localReports = datasourceReport.getAllActivityReports();

					for (int i = 0; i < reportsInfo.length(); i++) {
						JSONObject rpInfo = reportsInfo.getJSONObject(i);

						// check if this report is allready added.
						long startDate = rpInfo.getLong("startDate");
						boolean allreadyIn = false;
						System.out.println(localReports.size() + "   " + startDate);
						for (ActivityReportEntry rEntity : localReports) {
							if (startDate == rEntity.getStartDate()) {
								allreadyIn = true;
								System.out.println("already in this report");
								break;
							}
						}
						// add if is a new report.
						if (!allreadyIn) {
							ActivityReportEntry newReport = new ActivityReportEntry();
							newReport.setStartDate(rpInfo.getLong("startDate"));
							newReport.setEndDate(rpInfo.getLong("endDate"));
							newReport = datasourceReport.insertActivityReport(newReport);

							JSONArray activitiesInfo = rpInfo.getJSONArray("activities");
							for (int j = 0; j < activitiesInfo.length(); j++) {
								System.out.println("put activity");
								JSONObject acInfo = activitiesInfo.getJSONObject(j);
								ActivityCompressed newActivity = new ActivityCompressed();
								newActivity.setName(acInfo.getString("name"));
								newActivity.setValue(acInfo.getInt("value"));
								newActivity.setSteps(acInfo.getInt("steps"));
								newActivity.setReportID(newReport.getId());

								datasourceACompressed.insertActivityCompressed(newActivity);
							}
						}
					}
					datasourceACompressed.close();
					datasourceReport.close();
				}
				return jsonData.getString("message");
			} else {
				Log.e("JSON Response", "Failed to register command!");
				return "Connection error "+ statusCode;
			}

		} catch (JSONException ex) {
			ex.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Failed";
	}
}
