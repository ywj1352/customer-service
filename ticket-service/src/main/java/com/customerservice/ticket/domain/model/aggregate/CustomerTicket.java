package com.customerservice.ticket.domain.model.aggregate;

import com.customerservice.domain.model.entity.StaffProfile;
import com.customerservice.domain.model.valueobject.MessageSource;
import com.customerservice.ticket.domain.command.ApplyTicketCommand;
import com.customerservice.ticket.domain.command.FinishTicketCommand;
import com.customerservice.ticket.domain.command.ProcessTicketCommand;
import com.customerservice.ticket.domain.model.entity.Consultation;
import com.customerservice.ticket.domain.model.valueobject.Message;
import com.customerservice.ticket.domain.model.valueobject.TicketScore;
import com.customerservice.ticket.domain.model.valueobject.TicketStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 这里是聚合根 主要是处理工单的上下文
 * </p>
 * <p>
 *   业务介绍
 * - 用户在提交咨询时会生成一个客服工单，客服工单可以包括任意文字交互消息，但不能发送一些敏感词； <br/>
 * - 用户在申请客服工单时需要提供自己已购买商品所对应的订单信息，以及自己本次工单的述求，然后系统根据这些信息指定一个客服人员；<br/>
 * - 同一个用户在上一个客服工单没有完成之前无法申请新的工单，一个客服工单在同一时间也只能由一个客服进行处理；<br/>
 * - 如果用户不想咨询或投诉了，可以在任何时候关闭工单，但客服人员无权关闭工单；<br/>
 * - 工单的处理满意度表现为一种可以量化的分数，用户在结束工单时可以对工单进行评分 <br/>
 * </p>
 */
public class CustomerTicket {

    private TicketId ticketId;// 客服工单唯一编号
    /**
     * 一次CustomerTicket 必然只存在一次 Consultation
     * 所以这个是专项实体
     */
    private Consultation consultation;// 用户咨询
    private StaffProfile staff;// 客服人员
    private TicketStatus status;// 工单状态
    private List<Message> messages;// 工单详细消息列表
    private Message latestMessage;// 工单最近一条消息
    private TicketScore score;// 工单评分

    public CustomerTicket() {

    }

    public CustomerTicket(ApplyTicketCommand applyTicketCommand) {
        // 1.设置聚合标识符
        this.ticketId = new TicketId(applyTicketCommand.getTicketId());

        // 2.创建Consultation
        String consultationId = "Consultation" + UUID.randomUUID().toString().toUpperCase();
        this.consultation = new Consultation(consultationId, applyTicketCommand.getAccount(),
                applyTicketCommand.getOrder(), applyTicketCommand.getInquire());

        // 3.获取客服信息
        this.staff = applyTicketCommand.getStaff();

        // 4.初始化基础信息
        this.status = TicketStatus.INITIALIZED;
        this.messages = new ArrayList<Message>();
        this.score = new TicketScore(0);
    }

    public void processTicket(ProcessTicketCommand processTicketCommand) {

        // 验证TicketId是否对当前CustomerTicket对象是否有效
        String ticketId = processTicketCommand.getTicketId();
        if (!ticketId.equals(this.ticketId.getTicketId())) {
            return;
        }

        // 构建Message
        Message message = new Message(processTicketCommand.getTicketId(), this.consultation.getAccount(),
                this.staff.getStaffName(), processTicketCommand.getMessageSource(), processTicketCommand.getMessage());
        latestMessage = message;
        this.messages.add(message);

        // 更新工单状态
        this.status = TicketStatus.INPROCESS;
    }

    public void finishTicket(FinishTicketCommand finishTicketCommand) {
        // 构建Message
        Message message = new Message(finishTicketCommand.getTicketId(), this.consultation.getAccount(),
                this.staff.getStaffName(), MessageSource.CUSTOMER, finishTicketCommand.getMessage());
        latestMessage = message;
        this.messages.add(message);

        // 更新工单状态
        this.status = TicketStatus.CLOSED;

        // 设置工单评分
        this.score = new TicketScore(finishTicketCommand.getScore());
    }

    public TicketId getTicketId() {
        return ticketId;
    }

    public void setTicketId(TicketId ticketId) {
        this.ticketId = ticketId;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public StaffProfile getStaff() {
        return staff;
    }

    public void setStaff(StaffProfile staff) {
        this.staff = staff;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Message getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(Message latestMessage) {
        this.latestMessage = latestMessage;
    }

    public TicketScore getScore() {
        return score;
    }

    public void setScore(TicketScore score) {
        this.score = score;
    }
}
