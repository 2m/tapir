package tapir.client.sttp

import java.nio.ByteBuffer

import cats.effect.{ContextShift, IO}
import cats.implicits._
import com.softwaremill.sttp._
import com.softwaremill.sttp.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import tapir.Endpoint
import tapir.client.tests.ClientTests
import tapir.typelevel.ParamsAsArgs

import scala.concurrent.ExecutionContext

class SttpClientTests extends ClientTests[fs2.Stream[IO, ByteBuffer]] {
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.Implicits.global)
  private implicit val backend: SttpBackend[IO, fs2.Stream[IO, ByteBuffer]] = AsyncHttpClientFs2Backend[IO]()

  override def mkStream(s: String): fs2.Stream[IO, ByteBuffer] = fs2.Stream.emits(s.getBytes("utf-8")).map(b => ByteBuffer.wrap(Array(b)))
  override def rmStream(s: fs2.Stream[IO, ByteBuffer]): String =
    s.map(bb => fs2.Chunk.array(bb.array))
      .through(fs2.text.utf8DecodeC)
      .compile
      .foldMonoid
      .unsafeRunSync()

  override def send[I, E, O, FN[_]](e: Endpoint[I, E, O, fs2.Stream[IO, ByteBuffer]], port: Port, args: I)(
      implicit paramsAsArgs: ParamsAsArgs.Aux[I, FN]): IO[Either[E, O]] = {
    if (e.server.isDefined) {
      paramsAsArgs.applyFn(e.toSttpRequest, args).send().map(_.unsafeBody)
    } else {
      paramsAsArgs.applyFn(e.toSttpRequest(uri"http://localhost:$port"), args).send().map(_.unsafeBody)
    }
  }

  override protected def afterAll(): Unit = {
    backend.close()
    super.afterAll()
  }
}
