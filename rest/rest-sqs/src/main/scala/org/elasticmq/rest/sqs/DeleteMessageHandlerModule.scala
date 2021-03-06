package org.elasticmq.rest.sqs

import org.elasticmq.rest.RequestHandlerBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._

import Constants._
import org.elasticmq.rest.sqs.ActionUtil._
import org.elasticmq.MessageId

trait DeleteMessageHandlerModule { this: ClientModule with RequestHandlerLogicModule =>
  val deleteMessageLogic = logicWithQueue((queue, request, parameters) => {
    val id = parameters(ReceiptHandlerParameter)
    val messageOption = queue.lookupMessage(MessageId(id))
    // No failure even if the message doesn't exist
    messageOption.foreach(_.delete())

    <DeleteMessageResponse>
      <ResponseMetadata>
        <RequestId>{EmptyRequestId}</RequestId>
      </ResponseMetadata>
    </DeleteMessageResponse>
  })

  val DeleteMessageAction = createAction("DeleteMessage")

  val deleteMessageGetHandler = (createHandler
            forMethod GET
            forPath (QueuePath)
            requiringParameters List(ReceiptHandlerParameter)
            requiringParameterValues Map(DeleteMessageAction)
            running deleteMessageLogic)

  val deleteMessagePostHandler = (createHandler
            forMethod POST
            forPath (QueuePath)
            includingParametersFromBody()
            requiringParameters List(ReceiptHandlerParameter)
            requiringParameterValues Map(DeleteMessageAction)
            running deleteMessageLogic)
}