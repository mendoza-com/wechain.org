package com.boa.wechain.utils;

import com.boa.wechain.WechainApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Callback;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Boa (David Figueroa dgfigueroa29@gmail.com) on 18 oct 2017.
 */
public class Api{
	static Api it;
	RetrofitApiService retrofitApiService;
	public static final String BASE_URL = "https://api.wechain.org/v1/";
	public static final String REGISTER = "users/email";
	
	public static Api getIt(){
		if(it == null){
			it = new Api();
		}
		
		return it;
	}
	
	private Api(){
		try{
			OkHttpClient client;
			
			if(Common.DEBUG){
				HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
				interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
				client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
			}else{
				client = new OkHttpClient();
			}
			
			Gson gson = new GsonBuilder().create();
			Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).client(client).build();
			retrofitApiService = retrofit.create(RetrofitApiService.class);
		}catch(Exception e){
			Utils.logError(WechainApp.getContext(), "Api:construct - ", e);
		}
	}
	
	public void register(UserParam param, final ApiCallback callback){
		try{
			retrofitApiService.register(param).enqueue(getCallBackResponseBody(callback, "register"));
		}catch(Exception e){
			Utils.logError(WechainApp.getContext(), "Api:register - ", e);
		}
	}
	
	private static Callback<ResponseBody> getCallBackResponseBody(final ApiCallback callback, final String location){
		try{
			return new Callback<ResponseBody>(){
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
					try{
						if(response.isSuccessful()){
							callback.onLoaded(response.body());
						}else{
							callback.onConnectionError();
						}
					}catch(Exception e){
						Utils.logError(null, "Api:onResponse:"+location+" - ", e);
					}
				}
				
				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t){
					try{
						callback.onError(t);
					}catch(Exception e){
						Utils.logError(null, "Api:onFailure:"+location+" - ", e);
					}
				}
			};
		}catch(Exception e){
			Utils.logError(null, "Api:"+location+" - ", e);
		}
		
		return null;
	}
	
	public interface ApiCallback<T>{
		void onLoaded(T object);
		void onError(Throwable t);
		void onConnectionError();
	}
}