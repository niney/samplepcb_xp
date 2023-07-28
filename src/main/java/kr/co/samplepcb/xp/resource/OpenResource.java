package kr.co.samplepcb.xp.resource;

import coolib.common.CCResult;
import kr.co.samplepcb.xp.pojo.Alimtalk;
import kr.co.samplepcb.xp.service.AlimtalkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/open")
public class OpenResource {

    private final AlimtalkService alimtalkService;

    public OpenResource(AlimtalkService alimtalkService) {
        this.alimtalkService = alimtalkService;
    }

    @PostMapping("/_sendAlimtalk")
    public CCResult sendAlimtalk(@RequestBody Alimtalk alimtalk) {
        return this.alimtalkService.sendAlimtalk(alimtalk);
    }
}
