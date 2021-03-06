package org.elasticmq.rest.sqs

import org.elasticmq.rest.RequestHandlerBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._

import Constants._
import ActionUtil._
import MD5Util._
import ParametersParserUtil._
import org.elasticmq.{MessageBuilder, AfterMillisNextDelivery, Queue}

trait SendMessageHandlerModule { this: ClientModule with RequestHandlerLogicModule =>
  val sendMessageLogic = logicWithQueue((queue, request, parameters) => {
    val body = parameters(MessageBodyParameter)
    val delaySecondsOption = parameters.parseOptionalLong(DelaySecondsParameter)
    val messageToSend = createMessage(queue, body, delaySecondsOption)
    val message = queue.sendMessage(messageToSend)

    val digest = md5Digest(body)

    <SendMessageResponse>
      <SendMessageResult>
        <MD5OfMessageBody>{digest}</MD5OfMessageBody>
        <MessageId>{message.id.id}</MessageId>
      </SendMessageResult>
      <ResponseMetadata>
        <RequestId>{EmptyRequestId}</RequestId>
      </ResponseMetadata>
    </SendMessageResponse>
  })
  
  def createMessage(queue: Queue, body: String, delaySecondsOption: Option[Long]) = {
    val base = MessageBuilder(body)
    delaySecondsOption match {
      case None => base
      case Some(delaySeconds) => base.withNextDelivery(AfterMillisNextDelivery(delaySeconds*1000))
    }    
  }  

  val SendMessageAction = createAction("SendMessage")
  val MessageBodyParameter = "MessageBody"
  val DelaySecondsParameter = "DelaySeconds"

  val sendMessageGetHandler = (createHandler
            forMethod GET
            forPath (QueuePath)
            requiringParameters List(MessageBodyParameter)
            requiringParameterValues Map(SendMessageAction)
            running sendMessageLogic)

  val sendMessagePostHandler = (createHandler
            forMethod POST
            forPath (QueuePath)
            includingParametersFromBody ()
            requiringParameters List(MessageBodyParameter)
            requiringParameterValues Map(SendMessageAction)
            running sendMessageLogic)
}
