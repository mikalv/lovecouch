package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import dispatch.{Http, as}
import dispatch.stream.StringsByLine

object Requests {
  def buildQueryParameters(req:dispatch.Req, parameters:Seq[(String,String)]):dispatch.Req = {
    if(parameters.size == 0)
      return req

    buildQueryParameters(req.addQueryParameter(parameters.head._1, parameters.head._2), parameters.tail)
  }

  def buildHeaders(req:dispatch.Req, headers:Seq[(String,String)]):dispatch.Req = {
    if(headers.size == 0)
      return req

    buildHeaders(req.addHeader(headers.head._1, headers.head._2), headers.tail)
  }

  def buildBody(req:dispatch.Req, body:String):dispatch.Req = {
    if(body.size == 0)
      return req

    req << body
  }



  def get(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[String] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).GET, parameters.toSeq), headers.toSeq)
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
  }

  def getStream(url:String, callback:String => Unit, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Object with StringsByLine[Unit] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).GET, parameters.toSeq), headers.toSeq)
    val callBacker = as.stream.Lines(callback)
    Http(request > callBacker)
    callBacker
  }

  def post(url:String, body:String = "", parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[String] = {
    val request = buildBody(buildHeaders(buildQueryParameters(dispatch.url(url).POST, parameters.toSeq), headers.toSeq), body)
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
  }

  def put(url:String, body:String = "", parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[String] = {
    val request = buildBody(buildHeaders(buildQueryParameters(dispatch.url(url).PUT, parameters.toSeq), headers.toSeq), body)
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
  }

  def delete(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[String] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).DELETE, parameters.toSeq), headers.toSeq)
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
  }

}