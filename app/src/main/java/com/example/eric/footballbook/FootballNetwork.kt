package com.example.eric.footballbook

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.result.Result

class NetworkStuffs {

    private var g_login_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/HtmlHelper/NewLoginHandler.ashx"
    private var g_book_center_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/NewBookingCenter.aspx"
    private var g_book_stadium_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/NewBookingIndex.aspx"
    private var g_book_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/BookingSheet.aspx"
    private var g_book_image_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/GetImage.aspx"
    private var g_book_list_rec = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/HtmlHelper/GetRecordsList.ashx"
    private var g_book_detail_rec = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/BookingDetail.aspx"

    var ss: String = ""
    ///////////////////////////////////////////////////////////////
    // login function
    fun login(user: String?, pwd: String?) {
        Fuel.post(g_login_url, listOf("CardNo" to user, "PassWord" to pwd)).responseString {
                request, response, result ->
            when(result) {
                is Result.Success -> {
                    var data1 = result.get()
                    var session_id = response.headers["Set-Cookie"].toString()
                        .split(';')[0]
                        .split('[')[1]
                    visitBookCenter(session_id)
                }
                is Result.Failure -> {
                    val ex = result.getException()
                }
            }
        }
    }

    //
    private fun visitBookCenter(session_id: String) {
        Fuel.get(g_book_center_url)
            .header("Cookie" to session_id)
            .responseString() {
            request, response, result ->
            when(result) {
                is Result.Failure -> {
                }
                is Result.Success -> {
                    FootballUtils.analyzeViewForm(result.get())
                }
            }
        }
    }
}
