package com.loconet.backend.dto;

import com.loconet.backend.entity.UserIntent;
import lombok.Data;
import java.util.List;

@Data
public class ProfileUpdateRequest {
    private String organization;
    private UserIntent intent;
    private List<String> interests;
}
