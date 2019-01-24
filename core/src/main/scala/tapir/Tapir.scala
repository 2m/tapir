package tapir

import java.nio.charset.{Charset, StandardCharsets}

import tapir.GeneralCodec.{GeneralPlainCodec, PlainCodec}

trait Tapir {
  implicit def stringToPath(s: String): EndpointInput[Unit] = EndpointInput.PathSegment(s)

  def path[T: PlainCodec]: EndpointInput.PathCapture[T] =
    EndpointInput.PathCapture(implicitly[PlainCodec[T]], None, EndpointIO.Info.empty)
  def path[T: PlainCodec](name: String): EndpointInput.PathCapture[T] =
    EndpointInput.PathCapture(implicitly[PlainCodec[T]], Some(name), EndpointIO.Info.empty)
  def paths: EndpointInput.PathsCapture = EndpointInput.PathsCapture(EndpointIO.Info.empty)

  def query[T: GeneralPlainCodec](name: String): EndpointInput.Query[T] =
    EndpointInput.Query(name, implicitly[GeneralPlainCodec[T]], EndpointIO.Info.empty)
  def queryParams: EndpointInput.QueryParams = EndpointInput.QueryParams(EndpointIO.Info.empty)

  def header[T: GeneralPlainCodec](name: String): EndpointIO.Header[T] =
    EndpointIO.Header(name, implicitly[GeneralPlainCodec[T]], EndpointIO.Info.empty)
  def headers: EndpointIO.Headers = EndpointIO.Headers(EndpointIO.Info.empty)

  def body[T, M <: MediaType](implicit tm: GeneralCodec[T, M, _]): EndpointIO.Body[T, M, _] = EndpointIO.Body(tm, EndpointIO.Info.empty)

  def stringBody: EndpointIO.Body[String, MediaType.TextPlain, String] = stringBody(StandardCharsets.UTF_8)
  def stringBody(charset: String): EndpointIO.Body[String, MediaType.TextPlain, String] = stringBody(Charset.forName(charset))
  def stringBody(charset: Charset): EndpointIO.Body[String, MediaType.TextPlain, String] =
    EndpointIO.Body(GeneralCodec.stringCodec(charset), EndpointIO.Info.empty)

  def plainBody[T](implicit codec: GeneralCodec[T, MediaType.TextPlain, _]): EndpointIO.Body[T, MediaType.TextPlain, _] =
    EndpointIO.Body(codec, EndpointIO.Info.empty)
  def jsonBody[T](implicit codec: GeneralCodec[T, MediaType.Json, _]): EndpointIO.Body[T, MediaType.Json, _] =
    EndpointIO.Body(codec, EndpointIO.Info.empty)
  def binaryBody[T](implicit codec: GeneralCodec[T, MediaType.OctetStream, _]): EndpointIO.Body[T, MediaType.OctetStream, _] =
    EndpointIO.Body(codec, EndpointIO.Info.empty)
  def formBody[T](implicit codec: GeneralCodec[T, MediaType.XWwwFormUrlencoded, _]): EndpointIO.Body[T, MediaType.XWwwFormUrlencoded, _] =
    EndpointIO.Body(codec, EndpointIO.Info.empty)

  def streamBody[S](schema: Schema, mediaType: MediaType): StreamingEndpointIO.Body[S, mediaType.type] =
    StreamingEndpointIO.Body(CodecMeta(schema, mediaType), EndpointIO.Info.empty)

  def schemaFor[T: SchemaFor]: Schema = implicitly[SchemaFor[T]].schema

  case class InvalidOutput(reason: DecodeResult[Nothing], cause: Option[Throwable]) extends Exception(cause.orNull) // TODO

  val endpoint: Endpoint[Unit, Unit, Unit, Nothing] =
    Endpoint[Unit, Unit, Unit, Nothing](
      Method.GET,
      EndpointInput.Multiple(Vector.empty),
      EndpointIO.Multiple(Vector.empty),
      EndpointIO.Multiple(Vector.empty),
      EndpointInfo(None, None, None, Vector.empty)
    )
}
