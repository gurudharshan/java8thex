package com.team.examproject.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.team.examproject.entity.Accounts;
import com.team.examproject.entity.Mcq;
import com.team.examproject.repository.AccountsRepository;
import com.team.examproject.repository.McqsRepository;

@Controller
public class AccountsController {

	@Autowired
	AccountsRepository accountRepository;
	
	@Autowired
	McqsRepository mcqsRepository;
	
	@RequestMapping("home")
	public String home() {
        
		return "login";
	}

  @RequestMapping("welcome") 
  public String welcome(HttpSession session) {
      if(session.getAttribute("username") == null)
        return "redirect:/home";
    return "welcomepage";
  }

  @RequestMapping("mark")
  public String markAction(HttpSession session) {
    if(session.getAttribute("username") == null)
      return "redirect:/home";
    return "mark";
  }
    
	
	
	
	@PostMapping("/loginAction")                     
    public String saveDetails(@RequestParam("username") String username,
                              @RequestParam("password") String password,
                              ModelAndView modelandview,HttpSession session) {
		List<Accounts> allAccounts = accountRepository.findAll();
		for(Accounts a:allAccounts) {
			if(a.getAccname().equals(username) && a.getAccpass().equals(password)) {
				session.setAttribute("id", a.getId());
				session.setAttribute("username", username);

				if(a.getMarks()==-1) {
					
					return "redirect:/welcome";
				}
				else {
					
					session.setAttribute("mark",a.getMarks());
					return "redirect:/mark";
				}
				
			}

			
		}
		
		return "redirect:/home";
    }
	
	
	
	@PostMapping("/retake")
	 public ModelAndView retake(ModelAndView modelandview,HttpSession session) {
		System.out.println("retake");
		modelandview.addObject("username",session.getAttribute("username"));
		modelandview.setViewName("welcomepage");
		return modelandview;
	}
	
	@PostMapping("/logout")
	 public String logout(HttpSession session) {
		System.out.println("logout");
		session.removeAttribute("id");
		session.removeAttribute("username");
		session.removeAttribute("leaderboard");
		session.removeAttribute("mark");
		System.out.println(session.getAttribute("id"));
		return "redirect:/home";
	}
	
	@PostMapping("/mcqpage")
	public ModelAndView mcqpage(@RequestParam("lvlbtn") String btn, ModelAndView modelandview) {
		System.out.println("mcqpage");
		List<Mcq> questions = mcqsRepository.findByLvl(btn);
				modelandview.addObject("mcqs", questions);
		modelandview.setViewName("mcq");
		
		return modelandview;
	}
	
	@PostMapping("/markpage")
	public String markpage( HttpServletRequest request, ModelAndView modelandview,HttpSession session) {
		if(session.getAttribute("username")==null)
			return "redirect:/home";
		System.out.println("mark page");
		System.out.println(session.getAttribute("id"));
		int start=Integer.parseInt(request.getParameter("questionstarts"));
		int mark=0;
		String uservalue,correctvalue;
		for(int i=start;i<start+10;i++) {
			uservalue=request.getParameter(i+"");
			Mcq mcq = mcqsRepository.getById(i);
			correctvalue=mcq.getCorrectOption();
			if(uservalue.equals(correctvalue)) {
				mark++;
			}
		}
		int id=(int) session.getAttribute("id");
		Accounts useracc = accountRepository.getById(id);
		useracc.setMarks(mark);
		accountRepository.save(useracc);
		List<Accounts> lb=(List<Accounts>) accountRepository.getLeaderBoard();
		session.setAttribute("leaderboard", lb);
		session.setAttribute("mark", mark);
		modelandview.setViewName("mark");
		
		return "mark";
	}
	
}
