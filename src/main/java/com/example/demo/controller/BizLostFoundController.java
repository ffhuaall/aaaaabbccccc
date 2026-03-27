package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.document.EsLostFound;
import com.example.demo.entity.BizLostFound;
import com.example.demo.service.BizLostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lost-found")
public class BizLostFoundController {

    @Autowired
    private BizLostFoundService lostFoundService;

    /**
     * 发布失物/招领信息
     */
    @PostMapping("/publish")
    public Result<Boolean> publish(@RequestBody BizLostFound lostFound) {
        // 实际开发中，publisherId 应该从我们写的 JWT SecurityContext 里获取
        // 这里为了方便测试，如果你不传，默认给它分配个测试 ID = 1
        if (lostFound.getPublisherId() == null) {
            lostFound.setPublisherId(1L);
        }
        boolean success = lostFoundService.publishLostFound(lostFound);
        return Result.success(success);
    }

    /**
     * 关键字全文搜索 (调用 ES)
     */
    @GetMapping("/search")
    public Result<List<EsLostFound>> search(@RequestParam String keyword) {
        List<EsLostFound> result = lostFoundService.searchLostFound(keyword);
        return Result.success(result);
    }
}