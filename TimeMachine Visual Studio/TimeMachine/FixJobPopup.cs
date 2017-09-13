using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WindowsFormsApplication1
{
    public partial class FixJobPopup : Form
    {
        public string startKM { get; set; }
        public string endKM { get; set; }
        public string start { get; set; }
        public string end { get; set; }

        public FixJobPopup(string startKM, string endKM, string start, string end, string error)
        {
            InitializeComponent();
            textBox1.Text = this.startKM = startKM;
            textBox2.Text = this.endKM = endKM;
            textBox4.Text = this.start = start;
            textBox5.Text = this.end = end;

            Error.Text = error;

        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (String.IsNullOrEmpty(textBox1.Text) | String.IsNullOrEmpty(textBox2.Text) | String.IsNullOrEmpty(textBox4.Text) | String.IsNullOrEmpty(textBox5.Text))
            {
                MessageBox.Show("Empty textbox detected. Resetting to default values.");

                textBox1.Text = startKM;
                textBox2.Text = endKM;
                textBox4.Text = start;
                textBox5.Text = end;
            }
            else
            {

                startKM = textBox1.Text;
                endKM = textBox2.Text;
                start = textBox4.Text;
                end = textBox5.Text;
                this.DialogResult = DialogResult.OK;
                this.Close();
            }
        }
    }
}
