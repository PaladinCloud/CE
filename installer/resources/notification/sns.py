from resource.notification.function import InvokeNotificationFunction, SendNotificationFunction, TemplateFormatterFunction 



class NotificationSNS(SNSResoures):
    name = "notification-topic"
    DEPENDS_ON = []

class EmailSNS(SNSResoures):
    name = "email-topic"
    DEPENDS_ON = [InvokeNotificationFunction,SendNotificationFunction,TemplateFormatterFunction]

class NotificationSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  TemplateFormatterFunction.get_output_attr('arn')
    DEPENDS_ON = [NotificationSNS]

class EmailSubscription(SNSSubscription):
    topic_arn = EmailSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint = SendNotificationFunction.get_output_attr('arn')
    DEPENDS_ON = [EmailSNS,]