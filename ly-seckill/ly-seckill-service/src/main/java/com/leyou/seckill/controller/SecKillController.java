package com.leyou.seckill.controller;


import com.leyou.seckill.dto.SeckillPolicyDTO;

import com.leyou.seckill.ectity.TbSeckillPolicy;
import com.leyou.seckill.service.impl.SecKillService;
import com.leyou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀 后台管理操作
 *
 * @author
 */
@RestController
public class SecKillController {
    @Autowired
    private SecKillService secKillService;

    /**
     * 分页查询 秒杀商品
     *
     * @param page
     * @param rows
     * @param key
     * @param state 1-未开始 2-进行中  3-已结束
     * @return
     */
    @GetMapping("/findSecKillPage")
    public ResponseEntity<PageResult<SeckillPolicyDTO>> findSecKillByPage(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                                                          @RequestParam(name = "rows", defaultValue = "5") Integer rows,
                                                                          @RequestParam(name = "key", required = false) String key,
                                                                          @RequestParam(name = "state", required = false) Integer state) {
        return ResponseEntity.ok(secKillService.findSecKillByPage(page, rows, key, state));
    }

    /**
     * 根据id查询秒杀信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<SeckillPolicyDTO> findSecKillPolicyById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(secKillService.findSecKillById(id));
    }

    /**
     * 添加秒杀商品
     *
     * @param seckillPolicy
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addSecKill(@RequestBody TbSeckillPolicy seckillPolicy) {
        secKillService.addSecKill(seckillPolicy);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 修改秒杀商品
     *
     * @param seckillPolicy
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateSecKill(@RequestBody TbSeckillPolicy seckillPolicy) {
        secKillService.updateSecKill(seckillPolicy);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除秒杀商品
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSecKill(@PathVariable(name = "id") Long id) {
        secKillService.deleteSecKill(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @GetMapping(value = "/list/{date}",name = "根据日期查询秒杀商品")
    public ResponseEntity<List<SeckillPolicyDTO>> findSecKillPolicyList(@PathVariable("date") String date){
        List<SeckillPolicyDTO> seckillPolicyDTOS=secKillService.findSecKillPolicyList(date);
        return ResponseEntity.ok(seckillPolicyDTOS);
    }

}
