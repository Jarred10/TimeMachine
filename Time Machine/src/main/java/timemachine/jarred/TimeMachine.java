package timemachine.jarred;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.support.ui.Select;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

public class TimeMachine
{
    public static void main( String[] args ) throws InterruptedException, IOException {
        File data = new File("C:\\Users\\Jarred\\Documents\\Visual Studio 2015\\Projects\\OneTimeCrawler\\OneTimeCrawler\\bin\\Debug\\data");
        for(File fileEntry : data.listFiles()){
            if(fileEntry.isFile() && fileEntry.getName().endsWith(".txt")){


                FileReader fr = new FileReader(fileEntry.getPath());
                BufferedReader br = new BufferedReader(fr);

                Calendar today = Calendar.getInstance();

                String[] name = fileEntry.getName().split("-");
                Calendar timesheetCalendar = Calendar.getInstance();

                SimpleDateFormat expenseDateFormat = new SimpleDateFormat("dd/MM/YYYY");
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("EEEEEE, MMMMMM dd, yyyy");

                int year = parseInt("20" + name[2].split("\\.")[0]);
                int month = parseInt(name[1]) - 1;
                int date = parseInt(name[0]);
                timesheetCalendar.set(year, month, date);

                System.out.println(expenseDateFormat.format(timesheetCalendar.getTime()));

                WebDriver driver = new FirefoxDriver();
                WebDriverWait wait = new WebDriverWait(driver, 5);
                int dif = today.get(Calendar.MONTH) - timesheetCalendar.get(Calendar.MONTH);
                int dayOfWeek = timesheetCalendar.get(Calendar.DAY_OF_WEEK);

                while(true){
                    String jobNumber = br.readLine();
                    if(jobNumber == null) break;
                    String subject = br.readLine();
                    String site = br.readLine();
                    String jobType = br.readLine();
                    String startTime = br.readLine();
                    String endTime = br.readLine();
                    String startKM = br.readLine();
                    String endKM = br.readLine();
                    br.readLine();

                    boolean overtime = (parseInt(startTime.split(":")[0]) >= 17) || ((parseInt(startTime.split(":")[0]) < 8) && parseInt(startTime.split(":")[1]) < 30) || (dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY);


                    driver.get("https://JarredG:K1ngGe0rge!@onetime.onedatacom.com/Home/ShowJobs");

                    if(dif == 0){
                        driver.findElement(By.xpath("//div[@id='cal']/div[1]/a[1]")).click();
                        driver.findElement(By.xpath("//div[@id='cal']/div[1]/a[3]")).click();
                    }
                    while(dif > 0){
                        driver.findElement(By.xpath("//div[@id='cal']/div[1]/a[1]")).click();
                        dif -= 1;
                    }

                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@title='" + timeDateFormat.format(timesheetCalendar.getTime()) + "']"))).click();
                    WebElement baseTable = driver.findElement(By.xpath("//div[@id='onejobgrid']/div[2]/table"));

                    List<WebElement> clientName = baseTable.findElements(By.className("favClientName"));

                    boolean clicked = false;

                    if(jobType != "Unknown"){
                        for (WebElement e : clientName){
                            if (e.getText().contains(jobType)){
                                e.click();
                                jobType = e.getText();
                                clicked = true;
                                break;
                            }
                        }
                    }

                    if(!clicked) {

                        List<WebElement> clientDescription = baseTable.findElements(By.className("favParentDesc"));
                        List<String> tableRowsValues = new ArrayList<String>();

                        int commonSubIndex = 0;
                        int longestSubstring = -1;
                        for (int i = 0; i < clientName.size(); i++) {
                            String clientNameString = clientName.get(i).getText() + " - " + clientDescription.get(i).getText();
                            int result = longestSubstr(clientNameString, subject);
                            if (result > longestSubstring){
                                longestSubstring = result;
                                commonSubIndex = i;
                            }
                            tableRowsValues.add(clientNameString);
                        }
                        JFrame frame = new JFrame();
                        frame.setAlwaysOnTop(true);
                        Object selectedValue = JOptionPane.showInputDialog(frame,
                                "Job: " + subject,
                                "Input",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                tableRowsValues.toArray(),
                                tableRowsValues.toArray()[commonSubIndex]);

                        if (selectedValue == null) {
                            Select select = new Select(driver.findElement(By.id("clientDropDownList")));
                            jobType = select.getFirstSelectedOption().getText();

                        } else {
                            int index = tableRowsValues.indexOf(selectedValue);
                            clientName.get(index).click();
                            jobType = clientName.get(index).getText();
                        }
                        frame.dispose();
                    }

                    WebElement element = null;
                    element = driver.findElement(By.id("timeStartTxtBox"));
                    element.clear();
                    element.sendKeys(startTime);

                    element = driver.findElement(By.id("timeEndTxtBox"));
                    element.clear();
                    element.sendKeys(endTime);

                    element = driver.findElement(By.id("refOnSiteChkBox"));
                    if(!element.isSelected()) element.click();

                    if(overtime){
                        element = driver.findElement(By.id("refAfterChkBox"));
                        if(!element.isSelected()) element.click();
                        element = driver.findElement(By.id("refOvertimeChkBox"));
                        if(!element.isSelected()) element.click();
                    }

                    element = driver.findElement(By.id("refRefTxtBox"));
                    element.clear();
                    element.sendKeys(jobNumber);

                    element = driver.findElement(By.id("refJobCmtsTxtBox"));
                    element.clear();
                    element.sendKeys(subject);

                    element = driver.findElement(By.id("refIntCmtsTxtBox"));
                    element.clear();
                    element.sendKeys(site);

                    driver.findElement(By.id("saveBtn")).click();



                    if(overtime){
                        element = driver.findElement(By.id("callOutLblChkBox"));
                        if(!(element.isSelected()) && element.isDisplayed()){
                            wait.until(ExpectedConditions.elementToBeClickable(By.id("callOutLblChkBox"))).click();
                            wait.until(ExpectedConditions.elementToBeClickable(By.id("saveOnCallButton"))).click();
                        }
                    }

                    TimeUnit.SECONDS.sleep(1);

                    if(!StringUtils.isEmpty(startKM) && !StringUtils.isEmpty(endKM)){
                        driver.get("https://jarredg:K1ngGe0rge!@oneexpense.onedatacom.com/Home/ExpenseClaims");
                        wait.until(ExpectedConditions.elementToBeClickable(By.id("newLineBtn"))).click();
                        element = wait.until(ExpectedConditions.elementToBeClickable( By.id("lineDate")));
                        element.clear();
                        element.sendKeys(expenseDateFormat.format(timesheetCalendar.getTime()));
                        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='expenseTypeWrapper']/div[1]/div[1]/span[1]"))).click();
                        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//ul[@class='t-reset']/li[text()='Mileage']"))).click();

                        driver.findElement(By.id("lineDescriptionTextBox")).sendKeys(subject);
                        driver.findElement(By.id("startOdometerTextBox")).sendKeys(startKM);
                        driver.findElement(By.id("endOdometerTextBox")).sendKeys(endKM);

                        driver.findElement(By.id("codeToJobBtn")).click();

                        Select client = new Select(driver.findElement(By.id("clientDropDownList")));

                        int closestDifferenceIndex = 0;
                        int closestDifference = -1;
                        for(int i = 0; i < client.getOptions().size(); i++){
                            int result = longestSubstr(client.getOptions().get(i).getText(), jobType);
                            if( result > closestDifference){
                                closestDifference = result;
                                closestDifferenceIndex = i;
                            }
                        }

                        client.selectByIndex(closestDifferenceIndex);
                        List<WebElement> selectedClient = client.getAllSelectedOptions();

                        wait.until(ExpectedConditions.elementSelectionStateToBe(selectedClient.get(0), true));
                        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("saveBtn")))).click();

                        TimeUnit.SECONDS.sleep(1);


                    }




                }

                driver.close();
                br.close();
                fr.close();
            }

            fileEntry.delete();
            fileEntry = null;
        }
        data = null;
    }

    public static int longestSubstr(String first, String second) {
        int maxLen = 0;
        int fl = first.length();
        int sl = second.length();
        int[][] table = new int[fl+1][sl+1];

        for (int i = 1; i <= fl; i++) {
            for (int j = 1; j <= sl; j++) {
                if (first.charAt(i-1) == second.charAt(j-1)) {
                    table[i][j] = table[i - 1][j - 1] + 1;
                    if (table[i][j] > maxLen)
                        maxLen = table[i][j];
                }
            }
        }
        return maxLen;
    }
}








