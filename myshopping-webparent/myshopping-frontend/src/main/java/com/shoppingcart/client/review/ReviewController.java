package com.shoppingcart.client.review;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shoppingcart.client.ControllerHelper;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.common.entity.Review;
import com.shoppingcart.common.entity.product.Product;
import com.shoppingcart.common.exception.ProductNotFoundException;
import com.shoppingcart.common.exception.ReviewNotFoundException;
import com.shoppingcart.client.product.ProductService;
import com.shoppingcart.client.review.vote.ReviewVoteService;

@Controller
public class ReviewController {
	
	private String defaultRedirectURL = "redirect:/reviews/page/1?sortField=reviewTime&sortDir=desc";
	
	@Autowired private ReviewService reviewService;
	@Autowired private ControllerHelper controllerHelper;
	@Autowired private ProductService productService;
	@Autowired private ReviewVoteService voteService;
	
	//khi nhấn vào menu reviews 
	@GetMapping("/reviews")
	public String listFirstPage(Model model) {
		return defaultRedirectURL;
	}
	
	@GetMapping("/reviews/page/{pageNum}") 
	public String listReviewsByCustomerByPage(Model model, HttpServletRequest request,
							@PathVariable(name = "pageNum") int pageNum,
							String keyword, String sortField, String sortDir) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		Page<Review> page = reviewService.listByCustomerByPage(customer, keyword, pageNum, sortField, sortDir);		
		List<Review> listReviews = page.getContent();
		
		long startCount = (pageNum - 1) * ReviewService.REVIEWS_PER_PAGE + 1;
		model.addAttribute("startCount", startCount);
		
		long endCount = startCount + ReviewService.REVIEWS_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		model.addAttribute("moduleURL", "/reviews");
		model.addAttribute("listReviews", listReviews);
		model.addAttribute("endCount", endCount);
		
		return "reviews/reviews_customer";
	}
	
	//khi nhấn vào detail hoặc edit
	@GetMapping("/reviews/detail/{id}")
	public String viewReview(@PathVariable("id") Integer id, Model model, 
			RedirectAttributes ra, HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		try {
			Review review = reviewService.getByCustomerAndId(customer, id);
			model.addAttribute("review", review);
			
			return "reviews/review_detail_modal";
		} catch (ReviewNotFoundException ex) {
			ra.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;		
		}
	}
	
	//khi nhấn vào Sort by most voted trong trang reviews_votes
	//có 2 loại sort: votes và reviewTime
	@GetMapping("/ratings/{productAlias}/page/{pageNum}") 
	public String listByProductByPage(Model model,
				@PathVariable(name = "productAlias") String productAlias,
				@PathVariable(name = "pageNum") int pageNum,
				String sortField, String sortDir,
				HttpServletRequest request) {
		
		Product product = null;
		
		try {
			product = productService.getProduct(productAlias);
		} catch (ProductNotFoundException ex) {
			return "error/404";
		}
		
		Page<Review> page = reviewService.listByProduct(product, pageNum, sortField, sortDir);
		List<Review> listReviews = page.getContent();
		
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		if (customer != null) {
			voteService.markReviewsVotedForProductByCustomer(listReviews, product.getId(), customer.getId());//nếu customer hiện tại vote up thì sẽ in đậm thumb up hoặc nếu customer hiện tại vote down thì sẽ in đậm thumb down 
		}
		
		long startCount = (pageNum - 1) * ReviewService.REVIEWS_PER_PAGE + 1;
		model.addAttribute("startCount", startCount);
		
		long endCount = startCount + ReviewService.REVIEWS_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		model.addAttribute("listReviews", listReviews);
		model.addAttribute("product", product);
		model.addAttribute("endCount", endCount);
		model.addAttribute("pageTitle", "Reviews for " + product.getShortName());
		
		return "reviews/reviews_product";
	}
	
	//khi nhấn vào View all ratings trong trang product_top_reviews
	@GetMapping("/ratings/{productAlias}")
	public String listByProductFirstPage(@PathVariable(name = "productAlias") String productAlias, Model model,
			HttpServletRequest request) {
		return listByProductByPage(model, productAlias, 1, "reviewTime", "desc", request);
	}	
	
	//khi nhấn vào Write Review trong trang order_details_modal hoặc Write Your Review Now trong product_top_reviews
	@GetMapping("/write_review/product/{productId}")
	public String showViewForm(@PathVariable("productId") Integer productId, Model model,
			HttpServletRequest request) {
		Review review = new Review();
		Product product = null;
		
		try {
			product = productService.getProduct(productId);
		} catch (ProductNotFoundException ex) {
			return "error/404";
		}
		
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		boolean customerReviewed = reviewService.didCustomerReviewProduct(customer, product.getId());
		
		//nếu customerReviewed = true -->You already reviewed this product.
		//nếu customerCanReview = true -->có thể viết review cho product này
		if (customerReviewed) {
			model.addAttribute("customerReviewed", customerReviewed);
		} else {
			boolean customerCanReview = reviewService.canCustomerReviewProduct(customer, product.getId());
			
			if (customerCanReview) {
				model.addAttribute("customerCanReview", customerCanReview);				
			} else {
				model.addAttribute("NoReviewPermission", true);
			}
		}		
		
		model.addAttribute("product", product);
		model.addAttribute("review", review);
		
		return "reviews/review_form";
	}
	
	//khi customer viết review và nhấn submit 
	@PostMapping("/post_review")
	public String saveReview(Model model, Review review, Integer productId, HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		Product product = null;
		
		try {
			product = productService.getProduct(productId);
		} catch (ProductNotFoundException ex) {
			return "error/404";
		}
		
		review.setProduct(product);//gán product này thuộc về review hiện tại
		review.setCustomer(customer);//gán customer này thuộc về review hiện tại
		
		Review savedReview = reviewService.save(review);
		
		model.addAttribute("review", savedReview);
		
		return "reviews/review_done";
	}
}
