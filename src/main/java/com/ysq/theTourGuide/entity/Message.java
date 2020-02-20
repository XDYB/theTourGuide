package com.ysq.theTourGuide.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tourist_id")
    private Long touristId;

    private String message;

    /**
     * 1 为已查看，0为未查看
     */
    private Byte state;

    public static final String ID = "id";

    public static final String TOURIST_ID = "touristId";

    public static final String MESSAGE = "message";

    public static final String STATE = "state";

    public Message(Long touristId){
        this.touristId = touristId;
        this.state = 0;
    }

    public Message(Long touristId,String message){
        this.touristId = touristId;
        this.message = message;
    }

    public Message(Long id,Byte state){
        this.id = id;
        this.state = state;
    }
}