using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Forms;
using Outlook = Microsoft.Office.Interop.Outlook;



namespace WindowsFormsApplication1
{
    public partial class Form1 : Form
    {
        Outlook.Application olApp;
        Outlook.NameSpace olNs;
        Regex jobNumberRegex = new Regex(@"SV\d{10}", RegexOptions.IgnoreCase);

        string[] jobTypes = new string[]{ "Foodstuffs North Island", "NZ Lotteries Commission", "NZ Post Group", "Accident Compensation Corporation" };
        string[] jobTypePatterns = new string[] { "(?i)Alliance Site Code: 1639", "(?i)Company: Lotto", "(?i)Alliance Site Code: 4611NZPTWNCORP", "Alliance Site Code: 6018ACC011" };


        public Form1()
        {
            InitializeComponent();
            olApp = new Outlook.Application();

            olNs = olApp.GetNamespace("MAPI");
            olNs.Logon();

        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (monthCalendar1.SelectionRange.Start != null)
            {
                DateTime selected = monthCalendar1.SelectionRange.Start;
                Outlook.Items calendarSearch = olNs.GetDefaultFolder(Outlook.OlDefaultFolders.olFolderCalendar).Items.Restrict("[Start] >= '" + selected.ToShortDateString() + "' AND [End] <= '" + selected.AddDays(1).ToShortDateString() + "'");
                calendarSearch = calendarSearch.Restrict("@SQL=" + quote("urn:schemas:httpmail:subject") + " LIKE '%SV%'");
                calendarSearch.Sort("[Start]");

                string path = string.Format("data\\{0}-{1}-{2}.txt", selected.Day, selected.Month, selected.Year);
                File.Create(path).Dispose();
                TextWriter tw = new StreamWriter(path);

                foreach (Outlook.AppointmentItem ai in calendarSearch)
                {
                    String jobNumber = jobNumberRegex.Match(ai.Subject).Value;
                    Outlook.Items sentSearch = olNs.GetDefaultFolder(Outlook.OlDefaultFolders.olFolderSentMail).Items.Restrict("@SQL=" + quote("urn:schemas:httpmail:subject") + " LIKE '%" + jobNumber + "%'");
                    int index = 0;
                    if (sentSearch.Count > 1)
                    {
                        Form2 form = new Form2();
                        Control[] labelItems = form.Controls.Find("label1", true);
                        Label lbl = (Label)labelItems[0];
                        lbl.Text = string.Format("Select job for: {0}. Date: {1}", ai.Subject, ai.Start);
                        foreach (Outlook.MailItem mi in sentSearch)
                        {
                            Control[] listboxItems = form.Controls.Find("listBox1", true);
                            ListBox box = (ListBox)listboxItems[0];
                            box.Items.Add(string.Format("Subject: {0} Sent on: {1} \n Contents: {2}", mi.Subject, mi.SentOn, mi.Body));
                        }
                        var dialogResult = form.ShowDialog();
                        if (dialogResult == DialogResult.OK)
                        {
                            index = form.index;
                        }
                        else
                        {
                            MessageBox.Show("Invalid job selected. Try again.");
                            break;
                        }

                    }
                    string jobType = "Unknown";

                    for(int i = 0; i < jobTypePatterns.Length; i++)
                    {
                        Match match = Regex.Match(ai.Body, jobTypePatterns[i]);
                        if(match.Success)
                        {
                            jobType = jobTypes[i];
                            break;
                        }
                    }


                    Outlook.MailItem selectedItem = sentSearch[index + 1];
                    using (StringReader reader = new StringReader(selectedItem.Body))
                    {
                        string line;

                        string[] lines = new string[6];
                        bool[] foundLines = new bool[6];
                        string[] patterns = new string[] { "(?i)km start:( )*", "(?i)km end:( )*", "(?i)travel to( )*", "(?i)onsite( )*", "(?i)offsite( )*", "(?i)travel away( )*" };
                        
                        Match match = null;

                        while ((line = reader.ReadLine()) != null)
                        {
                            for (int i = 0; i < foundLines.Length; i++)
                            {
                                if (!foundLines[i])
                                {
                                    match = Regex.Match(line, patterns[i]);
                                    if (match.Success)
                                    {
                                        lines[i] = line.Substring(match.Index + match.Value.Length);
                                        foundLines[i] = true;
                                        break;
                                    }
                                }
                            }
                        }
                        String start = (String.IsNullOrEmpty(lines[2]) ? lines[3] : lines[2]);
                        String end = (String.IsNullOrEmpty(lines[5]) ? lines[4] : lines[5]);
                        String[] startSplit = start.Split(':');
                        String[] endSplit = end.Split(':');
                        TimeSpan startTS = new TimeSpan(Convert.ToInt32(startSplit[0]) + 5, Convert.ToInt32(startSplit[1]), 0);
                        TimeSpan endTS = new TimeSpan(Convert.ToInt32(endSplit[0]), Convert.ToInt32(endSplit[1]), 0);

                        if(startTS.CompareTo(endTS) >= 0)
                        {
                            FixJobPopup fix = new FixJobPopup(lines[0], lines[1], start, end, "Start time later than end time.");
                            var dialogResult = fix.ShowDialog();
                            if (dialogResult == DialogResult.OK)
                            {
                                start = fix.start;
                                end = fix.end;
                                lines[0] = fix.startKM;
                                lines[1] = fix.endKM;
                            }
                            else
                            {
                                MessageBox.Show("Invalid results from job fixing dialog.");
                                break;
                            }
                        }

                        tw.WriteLine(jobNumber);
                        tw.WriteLine((selectedItem.Subject.Length > ai.Subject.Length ? selectedItem.Subject : ai.Subject));
                        tw.WriteLine(jobType);
                        tw.WriteLine(start);
                        tw.WriteLine(end);
                        tw.WriteLine(lines[0]);
                        tw.WriteLine(lines[1]);
                        tw.WriteLine();
                    }
                }

                tw.Close();
            }
            else
            {
                MessageBox.Show("Please select a valid date.");
            }
        }

        private string quote(string toQuote)
        {
            return (char)34 + toQuote + (char)34;
        }

        private void button2_Click(object sender, EventArgs e)
        {
            System.Diagnostics.Process.Start("CMD.exe", "java -jar TimeMachine.jar");
        }
    }
}
