package kaur.trial.fortumo;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NumberController {


	protected Log log = LogFactory.getLog(getClass());
	private final static long MAX_SUM = 10000000000L;

	@ResponseBody
	@RequestMapping("/")
	public String addNumber(HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
		String data;
		ServletContext context = request.getServletContext();
		try {
			data = request.getReader().readLine();
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "Error reading data from request";
		}
		log.info(data);
		ConcurrentLinkedQueue<Long> numbers = getOrInitializeQueue(context);
		if ("end".equals(data)) {
			processEndSignal(numbers, context);
		} else {
			try {
				Long number = Long.valueOf(data);
				numbers.add(number);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return e.getClass().getName().concat(" - ").concat(e.getMessage());
			}
			synchronized (numbers) {
				numbers.wait();
			}
			numbers.poll();
		}
		return getResult(context);
	}

	private void processEndSignal(Queue<Long> numbers, ServletContext context) {
		try {
			context.setAttribute(StringConstants.ERROR, null);
			context.setAttribute("result", addNumbers(numbers));
		} catch (Exception e) {
			context.setAttribute(StringConstants.ERROR, e);
		}
		synchronized (numbers) {
			numbers.notifyAll();
		}
	}

	private Long addNumbers(Queue<Long> numbers) {
		Long result = numbers.stream().mapToLong(Long::valueOf).sum();
		if (result > MAX_SUM) {
			throw new IllegalArgumentException(StringConstants.SUM_TOO_LARGE);
		}
		return result;
	}

	private String getResult(ServletContext context) {
		Exception e = context.getAttribute(StringConstants.ERROR) != null ? (Exception) context.getAttribute(StringConstants.ERROR) : null;
		if (e != null) {
			return e.getMessage();
		} else {
			return  context.getAttribute("result").toString();
		}
	}

	@SuppressWarnings("unchecked")
	private ConcurrentLinkedQueue<Long> getOrInitializeQueue(ServletContext context) {
		synchronized (context) {
			Object numbersObject = context.getAttribute("numbers");
			if (numbersObject != null && numbersObject instanceof Queue<?>) {
				return (ConcurrentLinkedQueue<Long>) numbersObject;
			} else {
				ConcurrentLinkedQueue<Long> numbers = new ConcurrentLinkedQueue<>();
				context.setAttribute("numbers", numbers);
				return numbers;
			}
		}
	}
}
