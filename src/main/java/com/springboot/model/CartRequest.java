package com.springboot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CartRequest {

        int cartValue;
        int userId;
         int restaurantId;
}
