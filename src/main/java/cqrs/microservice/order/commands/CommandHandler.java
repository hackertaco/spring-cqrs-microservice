package cqrs.microservice.order.commands;

public interface CommandHandler {
    public String handle(CreateOrderCommand command);
}
